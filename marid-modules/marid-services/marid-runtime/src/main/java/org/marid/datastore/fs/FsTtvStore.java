/*
 * Copyright (C) 2013 Dmitry Ovchinnikov
 * Marid, the free data acquisition and visualization software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.marid.datastore.fs;

import com.google.common.base.Predicate;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.Sets;
import groovy.json.JsonOutput;
import org.marid.datastore.TtvStore;
import org.marid.io.SafeResult;
import org.marid.io.SimpleSafeResult;
import org.marid.service.ParameterizedMaridService;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.nio.channels.FileLock;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import static com.google.common.base.StandardSystemProperty.USER_HOME;
import static java.lang.Integer.parseInt;
import static java.nio.file.Files.newDirectoryStream;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.util.Calendar.*;
import static org.marid.groovy.GroovyRuntime.cast;
import static org.marid.methods.LogMethods.*;
import static org.marid.methods.PropMethods.get;
import static org.marid.nio.FileUtils.listSorted;

/**
 * @author Dmitry Ovchinnikov
 */
public class FsTtvStore extends ParameterizedMaridService implements TtvStore, FileVisitor<Path> {

    private static final Logger LOG = Logger.getLogger(FsTtvStore.class.getName());

    private final Path dir;
    private final Set<String> tagSet = new ConcurrentSkipListSet<>();
    private final AtomicLong usedSize = new AtomicLong();
    private final WatchService watchService;
    private final Cache<Path, FsTtvEntry> entryCache;
    private final int bufferSize;

    public FsTtvStore(Map params) throws IOException {
        super(params);
        bufferSize = get(params, int.class, "bufferSize", 1024);
        final Path defaultPath = Paths.get(USER_HOME.value(), "marid", "data");
        dir = get(params, Path.class, "dir", defaultPath).toAbsolutePath();
        watchService = FileSystems.getDefault().newWatchService();
        if (Files.notExists(dir)) {
            Files.createDirectories(dir);
        }
        entryCache = CacheBuilder.newBuilder()
                .maximumSize(get(params, long.class, "cacheSize", 1024L))
                .concurrencyLevel(get(params, int.class, "cacheConcurrency",
                        Runtime.getRuntime().availableProcessors()))
                .expireAfterAccess(get(params, long.class, "cacheExpirationTime", 1L),
                        get(params, TimeUnit.class, "cacheTimeUnit", TimeUnit.HOURS))
                .removalListener(new RemovalListener<Path, FsTtvEntry>() {
                    @Override
                    public void onRemoval(RemovalNotification<Path, FsTtvEntry> e) {
                        try {
                            final FsTtvEntry de = e.getValue();
                            if (de != null) {
                                synchronized (de) {
                                    long size;
                                    try {
                                        size = de.ch.size();
                                    } catch (Exception x) {
                                        size = 0L;
                                        warning(LOG, "Unable to get size: {0}", x, e.getKey());
                                    }
                                    de.close();
                                    try {
                                        if (size == 0L) {
                                            Files.delete(e.getKey());
                                            finest(LOG, "Deleted {0}", e.getKey());
                                        }
                                    } catch (Exception x) {
                                        warning(LOG, "Unable to delete {0}", x, e.getKey());
                                    }
                                }
                                fine(LOG, "Remove {0} from cache", e.getKey());
                            }
                        } catch (Exception x) {
                            warning(LOG, "Unable to close {0}", x, e.getKey());
                        }
                    }
                })
                .build();
        Files.walkFileTree(dir, this);
    }

    @Override
    protected void doStart() {
        notifyStarted();
        newThread(new Runnable() {
            @Override
            public void run() {
                try (final WatchService s = watchService) {
                    while (isRunning()) {
                        final WatchKey k = s.poll(1L, TimeUnit.SECONDS);
                        if (k == null) {
                            continue;
                        }
                        final Path parent = (Path) k.watchable();
                        for (final WatchEvent<?> ev : k.pollEvents()) {
                            try {
                                final Path path = parent.resolve((Path) ev.context());
                                final String name = path.getFileName().toString();
                                if (path.getParent().equals(dir)) {
                                    if (ev.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                                        tagSet.add(name);
                                        fine(LOG, "Added tag {0}", name);
                                    } else if (ev.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                                        tagSet.remove(name);
                                        fine(LOG, "Removed tag {0}", name);
                                    }
                                }
                            } catch (Exception x) {
                                warning(LOG, "Unable to process {0}", x, ev.context());
                            }
                        }
                        if (!k.reset()) {
                            k.cancel();
                            fine(LOG, "Cancelled {0}", parent);
                        }
                    }
                } catch (ClosedWatchServiceException x) {
                    fine(LOG, "Watch service has terminated");
                } catch (Exception x) {
                    warning(LOG, "Watch service error", x);
                }
            }
        }).start();

    }

    @Override
    protected void doStop() {
        entryCache.invalidateAll();
        entryCache.cleanUp();
        try {
            watchService.close();
            notifyStopped();
        } catch (IOException x) {
            notifyFailed(x);
        }
    }

    @Override
    public SafeResult<Set<String>> tagSet(final String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return new SimpleSafeResult<>(tagSet, Collections.<Throwable>emptyList());
        } else {
            return new SimpleSafeResult<>(Sets.<String>filter(tagSet, new Predicate<String>() {
                @Override
                public boolean apply(String input) {
                    return input.matches(pattern);
                }
            }), Collections.<Throwable>emptyList());
        }
    }

    private SafeResult<Map<String, Date>> getMaxMinTimestamp(Class<?> type, Set<String> tags, final boolean min) {
        final List<Throwable> errors = new LinkedList<>();
        final Map<String, Date> map = new TreeMap<>();
        TAG_LOOP:
        for (final String tag : tags) {
            final Path tagPath = dir.resolve(tag);
            if (Files.isDirectory(tagPath)) {
                try {
                    final TreeSet<Path> datePaths = listSorted(tagPath, "????-??-??.data");
                    for (final Path datePath : min ? datePaths : datePaths.descendingSet()) {
                        final AtomicBoolean flag = new AtomicBoolean();
                        access(datePath, new Accessor() {
                            @Override
                            public void access(FsTtvEntry de) throws Exception {
                                if (!de.records.isEmpty()) {
                                    map.put(tag, min ? de.records.firstKey() : de.records.lastKey());
                                    flag.set(true);
                                }
                            }
                        }, errors);
                        if (flag.get()) {
                            continue TAG_LOOP;
                        }
                    }
                } catch (Exception x) {
                    errors.add(x);
                }
            }
        }
        return new SimpleSafeResult<>(map, errors);
    }

    @Override
    public SafeResult<Map<String, Date>> getMinTimestamp(Class<?> type, Set<String> tags) {
        return getMaxMinTimestamp(type, tags, true);
    }

    @Override
    public SafeResult<Map<String, Date>> getMaxTimestamp(Class<?> type, Set<String> tags) {
        return getMaxMinTimestamp(type, tags, false);
    }

    @Override
    public <T> SafeResult<Map<String, NavigableMap<Date, T>>> after
            (Class<T> type, Set<String> tags, Date from, boolean inc) {
        return between(type, tags, from, inc, new Date(Long.MAX_VALUE), false);
    }

    @Override
    public <T> SafeResult<Map<String, NavigableMap<Date, T>>> before
            (Class<T> type, Set<String> tags, Date to, boolean inc) {
        return between(type, tags, new Date(Long.MIN_VALUE), false, to, inc);
    }

    @Override
    public <T> SafeResult<Map<String, NavigableMap<Date, T>>> between(
            final Class<T> type, Set<String> tags,
            final Date from, final boolean fromInc, final Date to, final boolean toInc) {
        final List<Throwable> errors = new LinkedList<>();
        final Map<String, NavigableMap<Date, T>> map = new TreeMap<>();
        for (final String tag : tags) {
            final Path tagPath = dir.resolve(tag);
            if (!Files.isDirectory(tagPath)) {
                continue;
            }
            final TreeMap<Date, Path> dateMap = new TreeMap<>();
            try (final DirectoryStream<Path> ds = newDirectoryStream(tagPath, "????-??-??.data")) {
                for (final Path dp : ds) {
                    final String name = dp.getFileName().toString();
                    final Calendar c = new GregorianCalendar(
                            parseInt(name.substring(0, 4)),
                            parseInt(name.substring(5, 7)) - 1,
                            parseInt(name.substring(8, 10)));
                    dateMap.put(c.getTime(), dp);
                }
            } catch (IOException x) {
                errors.add(x);
                warning(LOG, "Unable to enumerate the directory {0}", x, tagPath);
            }
            final NavigableMap<Date, T> values = new TreeMap<>();
            for (final Path dp : dateMap.subMap(from, true, to, true).values()) {
                access(dp, new Accessor() {
                    @Override
                    public void access(FsTtvEntry de) throws Exception {
                        for (final Entry<Date, FsTtvRecord> re : de.records.subMap(from, fromInc, to, toInc).entrySet()) {
                            final String text = de.text(re.getValue());
                            try {
                                final Object v;
                                if ("null".equals(text)) {
                                    v = null;
                                } else if (type == Double.class) {
                                    v = Double.valueOf(text);
                                } else if (type == Float.class) {
                                    v = Float.valueOf(text);
                                } else if (type == Long.class) {
                                    v = Long.valueOf(text);
                                } else if (type == Integer.class) {
                                    v = Integer.valueOf(text);
                                } else if (type == Boolean.class) {
                                    v = Boolean.valueOf(text);
                                } else if (type == Short.class) {
                                    v = Short.valueOf(text);
                                } else if (type == Byte.class) {
                                    v = Byte.valueOf(text);
                                } else if (type == BigDecimal.class) {
                                    v = new BigDecimal(text);
                                } else if (type == BigInteger.class) {
                                    v = new BigInteger(text);
                                } else {
                                    v = ((List) de.slurper.parseText("[" + text + "]")).get(0);
                                }
                                values.put(re.getKey(), cast(type, v));
                            } catch (Exception x) {
                                errors.add(x);
                            }
                        }
                    }
                }, errors);
            }
            map.put(tag, values);
        }
        return new SimpleSafeResult<>(map, errors);
    }

    @Override
    public long usedSize() {
        return usedSize.get();
    }

    private String dateFile(Calendar c) {
        return String.format("%04d-%02d-%02d.data", c.get(YEAR), c.get(MONTH) + 1, c.get(DATE));
    }

    public <T> SafeResult<Long> insert(
            Class<T> t, Map<String, Map<Date, T>> data, final boolean insert, final boolean update) {
        final List<Throwable> errors = new LinkedList<>();
        final GregorianCalendar calendar = new GregorianCalendar();
        long n = 0L;
        for (final Entry<String, Map<Date, T>> te : data.entrySet()) {
            final String tag = te.getKey();
            final Path tagPath = dir.resolve(tag);
            try {
                Files.createDirectories(tagPath);
            } catch (IOException x) {
                errors.add(x);
                continue;
            }
            for (final Entry<Date, T> e : te.getValue().entrySet()) {
                try {
                    calendar.setTime(e.getKey());
                    final Path dp = tagPath.resolve(dateFile(calendar));
                    final T value = cast(t, e.getValue());
                    final String v;
                    if (value instanceof Number) {
                        v = value.toString();
                    } else if (value instanceof String) {
                        v = JsonOutput.toJson((String) value);
                    } else if (value instanceof Boolean) {
                        v = value.toString();
                    } else if (value instanceof Map) {
                        v = JsonOutput.toJson((Map) value);
                    } else if (value instanceof URL) {
                        v = JsonOutput.toJson((URL) value);
                    } else if (value instanceof UUID) {
                        v = JsonOutput.toJson((UUID) value);
                    } else if (value instanceof Character) {
                        v = JsonOutput.toJson((Character) value);
                    } else if (value instanceof Date) {
                        v = JsonOutput.toJson((Date) value);
                    } else if (value instanceof Calendar) {
                        v = JsonOutput.toJson((Calendar) value);
                    } else {
                        v = JsonOutput.toJson(value);
                    }
                    access(dp, new Accessor() {
                        @Override
                        public void access(FsTtvEntry de) throws Exception {
                            de.put(e.getKey(), v, insert, update);
                        }
                    }, errors);
                    n++;
                } catch (Exception x) {
                    errors.add(x);
                }
            }
        }
        return new SimpleSafeResult<>(n, errors);
    }

    @Override
    public <T> SafeResult<Long> insert(Class<T> type, Map<String, Map<Date, T>> data) {
        return insert(type, data, true, false);
    }

    @Override
    public <T> SafeResult<Long> insertOrUpdate(Class<T> type, Map<String, Map<Date, T>> data) {
        return insert(type, data, true, true);
    }

    @Override
    public <T> SafeResult<Long> update(Class<T> type, Map<String, Map<Date, T>> data) {
        return insert(type, data, false, true);
    }

    @Override
    public SafeResult<Long> remove(Class<?> type, Set<String> tags) {
        final List<Throwable> errors = new LinkedList<>();
        long n = 0L;
        for (final String tag : tags) {
            final Path tagPath = dir.resolve(tag);
            if (!Files.isDirectory(tagPath)) {
                continue;
            }
            try (final DirectoryStream<Path> ds = newDirectoryStream(tagPath, "????-??-??.data")) {
                for (final Path dp : ds) {
                    access(dp, new Accessor() {
                        @Override
                        public void access(FsTtvEntry de) throws Exception {
                            de.ch.truncate(0L);
                        }
                    }, errors);
                    n++;
                    entryCache.invalidate(dp);
                }
            } catch (IOException x) {
                errors.add(x);
            }
            try {
                Files.deleteIfExists(tagPath);
            } catch (DirectoryNotEmptyException x) {
                warning(LOG, "Directory {0} is not empty", x.getFile());
            } catch (IOException x) {
                errors.add(x);
            }
        }
        return new SimpleSafeResult<>(n, errors);
    }

    @Override
    public SafeResult<Long> removeAfter(Class<?> type, Set<String> tags, Date from, boolean inc) {
        return removeBetween(type, tags, from, inc, new Date(Long.MAX_VALUE), false);
    }

    @Override
    public SafeResult<Long> removeBefore(Class<?> type, Set<String> tags, Date to, boolean inc) {
        return removeBetween(type, tags, new Date(Long.MIN_VALUE), false, to, inc);
    }

    @Override
    public SafeResult<Long> removeBetween(
            Class<?> type, Set<String> tags,
            final Date from, final boolean fromInc, final Date to, final boolean toInc) {
        final List<Throwable> errors = new LinkedList<>();
        long n = 0L;
        for (final String tag : tags) {
            final Path tagPath = dir.resolve(tag);
            if (!Files.isDirectory(tagPath)) {
                continue;
            }
            final TreeMap<Date, Path> dateMap = new TreeMap<>();
            try (final DirectoryStream<Path> ds = newDirectoryStream(tagPath, "????-??-??.data")) {
                for (final Path p : ds) {
                    final String name = p.getFileName().toString();
                    final Calendar calendar = new GregorianCalendar(
                            Integer.parseInt(name.substring(0, 4)),
                            Integer.parseInt(name.substring(5, 7)) - 1,
                            Integer.parseInt(name.substring(8, 10)));
                    dateMap.put(calendar.getTime(), p);
                }
            } catch (IOException x) {
                errors.add(x);
            }
            for (final Entry<Date, Path> e : dateMap.subMap(from, true, to, true).entrySet()) {
                access(e.getValue(), new Accessor() {
                    @Override
                    public void access(FsTtvEntry de) throws Exception {
                        de.remove(from, fromInc, to, toInc);
                    }
                }, errors);
            }
        }
        return new SimpleSafeResult<>(n, errors);
    }

    @Override
    public SafeResult<Long> removeKeys(Class<?> type, Map<String, Date> keys) {
        final List<Throwable> errors = new LinkedList<>();
        long n = 0L;
        for (final Entry<String, Date> e : keys.entrySet()) {
            try {
                final Calendar c = new GregorianCalendar();
                c.setTime(e.getValue());
                final Path tagPath = dir.resolve(e.getKey());
                if (!Files.isDirectory(tagPath)) {
                    continue;
                }
                try (final DirectoryStream<Path> ds = newDirectoryStream(tagPath, "????-??-??.data")) {
                    for (final Path dp : ds) {
                        final String name = dp.getFileName().toString();
                        if (parseInt(name.substring(0, 4)) != c.get(YEAR)) {
                            continue;
                        }
                        if (parseInt(name.substring(5, 7)) != c.get(MONTH) + 1) {
                            continue;
                        }
                        if (parseInt(name.substring(8, 10)) != c.get(DATE)) {
                            continue;
                        }
                        access(dp, new Accessor() {
                            @Override
                            public void access(FsTtvEntry de) throws Exception {
                                de.remove(e.getValue());
                            }
                        }, errors);
                        break;
                    }
                }
            } catch (Exception x) {
                errors.add(x);
            }
        }
        return new SimpleSafeResult<>(n, errors);
    }

    @Override
    public SafeResult<Long> clear() {
        return remove(Object.class, tagSet);
    }

    @Override
    public boolean isTypeSupported(Class<?> type) {
        return true;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public FileVisitResult preVisitDirectory(Path d, BasicFileAttributes attrs) throws IOException {
        if (Files.isHidden(d) || d.getFileName().toString().startsWith(".")) {
            return FileVisitResult.SKIP_SUBTREE;
        } else {
            if (dir.equals(d.getParent())) {
                tagSet.add(d.getFileName().toString());
            }
            return FileVisitResult.CONTINUE;
        }
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        usedSize.addAndGet(Files.size(file));
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        warning(LOG, "Visit {0} failed", exc, file);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        dir.register(watchService, ENTRY_CREATE, ENTRY_DELETE);
        fine(LOG, "Registered {0}", dir);
        return FileVisitResult.CONTINUE;
    }

    void access(final Path dp, Accessor acc, Collection<Throwable> errors) {
        try {
            final FsTtvEntry dateEntry = entryCache.get(dp, new Callable<FsTtvEntry>() {
                @Override
                public FsTtvEntry call() throws Exception {
                    return new FsTtvEntry(dp, bufferSize);
                }
            });
            synchronized (dateEntry) {
                final FileLock lock = dateEntry.ch.lock();
                try {
                    acc.access(dateEntry);
                } finally {
                    lock.release();
                }
            }
        } catch (Exception x) {
            errors.add(x);
            warning(LOG, "Unable to process {0}", x, dp);
        }
    }

    interface Accessor {

        void access(FsTtvEntry de) throws Exception;
    }
}
