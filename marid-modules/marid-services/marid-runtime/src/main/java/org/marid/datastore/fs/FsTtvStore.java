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

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.Sets;
import org.marid.datastore.TtvStore;
import org.marid.io.SafeResult;
import org.marid.io.SimpleSafeResult;
import org.marid.nio.FileUtils;
import org.marid.service.AbstractMaridService;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.spi.FileSystemProvider;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import static com.google.common.base.StandardSystemProperty.USER_HOME;
import static com.google.common.io.Files.getFileExtension;
import static java.lang.Integer.parseInt;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.newDirectoryStream;
import static java.nio.file.StandardOpenOption.*;
import static java.nio.file.StandardWatchEventKinds.*;
import static java.util.Calendar.*;
import static java.util.Collections.singletonMap;
import static java.util.Collections.unmodifiableSet;
import static org.marid.methods.LogMethods.*;
import static org.marid.methods.PropMethods.get;
import static org.marid.nio.FileUtils.listSorted;

/**
 * @author Dmitry Ovchinnikov
 */
public class FsTtvStore extends AbstractMaridService implements TtvStore, FileVisitor<Path> {

    private static final Logger LOG = Logger.getLogger(FsTtvStore.class.getName());

    private final Path dir;
    private final Set<String> tagSet = new ConcurrentSkipListSet<>();
    private final AtomicLong usedSize = new AtomicLong();
    private final Map<Class<?>, TypeHandler<?>> typeBinding = new IdentityHashMap<>();
    private final Map<String, TypeHandler<?>> typeExtBinding = new HashMap<>();
    private final Set<Class<?>> supportedTypes = unmodifiableSet(typeBinding.keySet());
    private final WatchService watchService;
    private final String extension;
    private final FileSystemProvider fileSystemProvider;
    private final Cache<Path, FileSystem> fileSystemCache;
    private final String dateGlob;
    private volatile int maximumFetchSize;
    private volatile int queryTimeout;

    @SuppressWarnings("unchecked")
    public FsTtvStore(Map params) throws IOException {
        super(params);
        final Path defaultPath = Paths.get(USER_HOME.value(), "marid", "data");
        final String scheme = get(params, String.class, "scheme", "jar");
        extension = get(params, String.class, "extension", scheme);
        dateGlob = "????-??-??." + extension;
        FileSystemProvider fsProvider = null;
        for (final FileSystemProvider provider : FileSystemProvider.installedProviders()) {
            if (Objects.equals(provider.getScheme(), scheme)) {
                fsProvider = provider;
                break;
            }
        }
        if (fsProvider == null) {
            throw new NoSuchElementException("No such file system provider: " + scheme);
        }
        fileSystemProvider = fsProvider;
        dir = get(params, Path.class, "dir", defaultPath).toAbsolutePath();
        watchService = FileSystems.getDefault().newWatchService();
        if (Files.notExists(dir)) {
            Files.createDirectories(dir);
        }
        fileSystemCache = CacheBuilder.newBuilder()
                .maximumSize(get(params, long.class, "cacheSize", 1024L))
                .concurrencyLevel(get(params, int.class, "cacheConcurrency",
                        Runtime.getRuntime().availableProcessors()))
                .expireAfterAccess(get(params, long.class, "cacheExpirationTime", 1L),
                        get(params, TimeUnit.class, "cacheTimeUnit", TimeUnit.HOURS))
                .removalListener(new RemovalListener<Path, FileSystem>() {
                    @Override
                    public void onRemoval(RemovalNotification<Path, FileSystem> e) {
                        try {
                            final FileSystem fs = e.getValue();
                            if (fs != null) {
                                boolean empty = false;
                                synchronized (fs) {
                                    try {
                                        final Path d = fs.getRootDirectories().iterator().next();
                                        try (final DirectoryStream<Path> s = newDirectoryStream(d)) {
                                            empty = !s.iterator().hasNext();
                                        }
                                    } catch (Exception x) {
                                        warning(LOG, "File system error", x);
                                    }
                                    fs.close();
                                }
                                if (empty) {
                                    try {
                                        Files.deleteIfExists(e.getKey());
                                        info(LOG, "Deleted {0}", e.getKey());
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
        typeBinding.put(Double.class, new DoubleTypeHandler());
        for (final Entry<Class<?>, TypeHandler<?>> e : typeBinding.entrySet()) {
            typeExtBinding.put(e.getValue().getExtension(), e.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    private <T> TypeHandler<T> getTypeHandler(Class<T> type) {
        return (TypeHandler<T>) typeBinding.get(type);
    }

    @SuppressWarnings("unchecked")
    private <T> TypeHandler<T> getTypeHandler(String ext, Class<T> type) {
        return (TypeHandler<T>) typeExtBinding.get(ext);
    }

    @Override
    protected void doStart() {
        notifyStarted();
    }

    @Override
    protected void doStop() {
        fileSystemCache.invalidateAll();
        notifyStopped();
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

    private Map<String, Date> getMaxMinTimestamp(Class<?> type, Set<String> tags, boolean min) {
        final Map<String, Date> map = new TreeMap<>();
        final String timeGlob;
        try {
            timeGlob = "??-??-??." + typeBinding.get(type).getExtension();
        } catch (NullPointerException x) {
            throw new IllegalArgumentException("Unsupported type: " + type, x);
        }
        try {
            TAG_LOOP:
            for (final String tag : tags) {
                final Path tagPath = dir.resolve(tag);
                if (Files.isDirectory(tagPath)) {
                    final TreeSet<Path> datePaths = listSorted(tagPath, dateGlob);
                    for (final Path datePath : min ? datePaths : datePaths.descendingSet()) {
                        final TreeSet<Path> timePaths = listSorted(datePath, timeGlob);
                        for (final Path timePath : min ? timePaths : timePaths.descendingSet()) {
                            final String time = timePath.getFileName().toString();
                            final String date = datePath.getFileName().toString();
                            final GregorianCalendar calendar = new GregorianCalendar(
                                    Integer.parseInt(date.substring(0, 4)),
                                    Integer.parseInt(date.substring(5, 7)) - 1,
                                    Integer.parseInt(date.substring(8, 10)),
                                    Integer.parseInt(time.substring(0, 2)),
                                    Integer.parseInt(time.substring(3, 5)),
                                    Integer.parseInt(time.substring(6, 8)));
                            map.put(tag, calendar.getTime());
                            continue TAG_LOOP;
                        }
                    }
                }
            }
            return map;
        } catch (IOException x) {
            throw new IllegalStateException(x);
        }
    }

    @Override
    public SafeResult<Map<String, Date>> getMinTimestamp(Class<?> type, Set<String> tags) {
        return new SimpleSafeResult<>(
                getMaxMinTimestamp(type, tags, true),
                Collections.<Throwable>emptyList());
    }

    @Override
    public SafeResult<Map<String, Date>> getMaxTimestamp(Class<?> type, Set<String> tags) {
        return new SimpleSafeResult<>(
                getMaxMinTimestamp(type, tags, false),
                Collections.<Throwable>emptyList());
    }

    @Override
    public void setMaximumFetchSize(int size) {
        maximumFetchSize = size;
    }

    @Override
    public int getMaximumFetchSize() {
        return maximumFetchSize;
    }

    @Override
    public int getQueryTimeout() {
        return queryTimeout;
    }

    @Override
    public void setQueryTimeout(int timeout) {
        queryTimeout = timeout;
    }

    @Override
    public <T> SafeResult<Map<String, NavigableMap<Date, T>>> after(Class<T> type, Set<String> tags, Date from, boolean inc) {
        return between(type, tags, from, inc, new Date(Long.MAX_VALUE), false);
    }

    @Override
    public <T> SafeResult<Map<String, NavigableMap<Date, T>>> before(Class<T> type, Set<String> tags, Date to, boolean inc) {
        return between(type, tags, new Date(Long.MIN_VALUE), false, to, inc);
    }

    @Override
    public <T> SafeResult<Map<String, NavigableMap<Date, T>>> between(Class<T> type, Set<String> tags, Date from, boolean fromInc, Date to, boolean toInc) {
        final List<Throwable> errors = new LinkedList<>();
        final Map<String, NavigableMap<Date, T>> map = new TreeMap<>();
        final Calendar f = new GregorianCalendar();
        final Calendar t = new GregorianCalendar();
        f.setTime(from);
        t.setTime(to);
        for (final String tag : tags) {
            final Path tagPath = dir.resolve(tag);
            if (!Files.isDirectory(tagPath)) {
                continue;
            }
            final NavigableMap<Date, T> values = new TreeMap<>();
            final TreeMap<Calendar, Path> dateMap = new TreeMap<>();
            try (final DirectoryStream<Path> ds = newDirectoryStream(tagPath, dateGlob)) {
                for (final Path dp : ds) {
                    final String name = dp.getFileName().toString();
                    final Calendar c = new GregorianCalendar(
                            parseInt(name.substring(0, 4)),
                            parseInt(name.substring(5, 7)),
                            parseInt(name.substring(8, 10)));
                    dateMap.put(c, dp);
                }
            } catch (IOException x) {
                errors.add(x);
                warning(LOG, "Unable to enumerate the directory {0}", x, tagPath);
            }
            for (final Entry<Calendar, Path> e : dateMap.subMap(f, true, t, true).entrySet()) {
                try {
                    final FileSystem fs = fileSystem(e.getValue());
                    final TreeMap<Calendar, Path> timeMap = new TreeMap<>();
                    synchronized (fs) {
                        final Path d = fs.getRootDirectories().iterator().next();
                        try (final DirectoryStream<Path> s = newDirectoryStream(d)) {
                            for (final Path tp : s) {
                                final String tpn = tp.getFileName().toString();
                                final Calendar c = new GregorianCalendar();
                                c.setTime(e.getKey().getTime());
                                c.set(HOUR_OF_DAY, parseInt(tpn.substring(0, 2)));
                                c.set(MINUTE, parseInt(tpn.substring(3, 5)));
                                c.set(SECOND, parseInt(tpn.substring(6, 8)));
                                timeMap.put(c, tp);
                            }
                        }
                        for (final Entry<Calendar, Path> te : timeMap.subMap(f, fromInc, t, toInc).entrySet()) {
                            try {
                                final String name = te.getValue().getFileName().toString();
                                final String ext = getFileExtension(name);
                                final TypeHandler<T> th = getTypeHandler(ext, type);
                                final byte[] data = Files.readAllBytes(te.getValue());
                                final String stringData = new String(data, UTF_8);
                                values.put(te.getKey().getTime(), th.parse(stringData));
                            } catch (Exception x) {
                                errors.add(x);
                                warning(LOG, "Unable to process {0}", x, te.getValue());
                            }
                        }
                    }
                } catch (Exception x) {
                    errors.add(x);
                    warning(LOG, "Unable to process {0}", x, e.getValue());
                }
            }
            map.put(tag, values);
        }
        return new SimpleSafeResult<>(map, errors);
    }

    @Override
    public long usedSize() {
        return usedSize.get();
    }

    private FileSystem fileSystem(final Path path) throws Exception {
        return fileSystemCache.get(path, new Callable<FileSystem>() {
            @Override
            public FileSystem call() throws Exception {
                return fileSystemProvider.newFileSystem(path, singletonMap("create", "true"));
            }
        });
    }

    private String dateFile(Calendar calendar) {
        return String.format("%04d-%02d-%02d.%s",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DATE),
                extension);
    }

    private String timeFile(Calendar calendar, String ext) {
        return String.format("%02d-%02d-%02d.%s",
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND),
                ext);
    }

    public <T> SafeResult<Long> insert(Class<T> t, Map<String, Map<Date, T>> data, Set<? extends OpenOption> o) {
        final List<Throwable> errors = new LinkedList<>();
        final TypeHandler<T> th = getTypeHandler(t);
        Preconditions.checkNotNull(th, "Unsupported type: " + t);
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
                final Date ts = e.getKey();
                final T v = e.getValue();
                final FileSystem fs;
                final Path timePath;
                try {
                    calendar.setTime(ts);
                    final Path datePath = tagPath.resolve(dateFile(calendar));
                    fs = fileSystem(datePath);
                    timePath = fs.getPath(timeFile(calendar, th.getExtension()));
                } catch (Exception x) {
                    errors.add(x);
                    continue;
                }
                try {
                    synchronized (fs) {
                        try (final FileChannel ch = fs.provider().newFileChannel(timePath, o)) {
                            ch.write(UTF_8.encode(th.toString(v)));
                        }
                    }
                    n++;
                } catch (NoSuchFileException x) {
                    if (!o.contains(CREATE)) {
                        errors.add(x);
                        continue;
                    }
                    final Set<? extends OpenOption> s = EnumSet.of(CREATE_NEW, WRITE);
                    try { // JDK Bug
                        synchronized (fs) {
                            try (final FileChannel ch = fs.provider().newFileChannel(timePath, s)) {
                                ch.write(UTF_8.encode(th.toString(v)));
                            }
                        }
                        n++;
                    } catch (Exception y) {
                        errors.add(x);
                    }
                } catch (Exception x) {
                    errors.add(x);
                }
            }
        }
        return new SimpleSafeResult<>(n, errors);
    }

    @Override
    public <T> SafeResult<Long> insert(Class<T> type, Map<String, Map<Date, T>> data) {
        return insert(type, data, EnumSet.of(CREATE_NEW, WRITE));
    }

    @Override
    public <T> SafeResult<Long> insertOrUpdate(Class<T> type, Map<String, Map<Date, T>> data) {
        return insert(type, data, EnumSet.of(CREATE, WRITE, TRUNCATE_EXISTING));
    }

    @Override
    public <T> SafeResult<Long> update(Class<T> type, Map<String, Map<Date, T>> data) {
        return insert(type, data, EnumSet.of(WRITE, TRUNCATE_EXISTING));
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
            try (final DirectoryStream<Path> ds = newDirectoryStream(tagPath, dateGlob)) {
                for (final Path dp : ds) {
                    try {
                        final FileSystem fs = fileSystem(dp);
                        synchronized (fs) {
                            for (final Path d : fs.getRootDirectories()) {
                                Files.walkFileTree(d, FileUtils.FILE_CLEANER);
                            }
                        }
                        fileSystemCache.invalidate(dp);
                    } catch (Exception x) {
                        errors.add(x);
                    }
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
    public SafeResult<Long> removeBetween(Class<?> type, Set<String> tags, Date from, boolean fromInc, Date to, boolean toInc) {
        final List<Throwable> errors = new LinkedList<>();
        long n = 0L;
        final Calendar f = new GregorianCalendar();
        final Calendar t = new GregorianCalendar();
        f.setTime(from);
        t.setTime(to);
        for (final String tag : tags) {
            final Path tagPath = dir.resolve(tag);
            if (!Files.isDirectory(tagPath)) {
                continue;
            }
            final TreeMap<Calendar, Path> dateMap = new TreeMap<>();
            try (final DirectoryStream<Path> ds = newDirectoryStream(tagPath, dateGlob)) {
                for (final Path p : ds) {
                    final String name = p.getFileName().toString();
                    final Calendar calendar = new GregorianCalendar(
                            Integer.parseInt(name.substring(0, 4)),
                            Integer.parseInt(name.substring(5, 7)) - 1,
                            Integer.parseInt(name.substring(8, 10)));
                    dateMap.put(calendar, p);
                }
            } catch (IOException x) {
                errors.add(x);
            }
            for (final Entry<Calendar, Path> de : dateMap.subMap(f, true, t, true).entrySet()) {
                final Path dp = de.getValue();
                try {
                    final FileSystem fs = fileSystem(dp);
                    final TreeMap<Calendar, Path> timeMap = new TreeMap<>();
                    synchronized (fs) {
                        final Path d = fs.getRootDirectories().iterator().next();
                        try (final DirectoryStream<Path> s = newDirectoryStream(d)) {
                            for (final Path tp : s) {
                                final String name = tp.getFileName().toString();
                                final Calendar calendar = new GregorianCalendar();
                                calendar.setTime(de.getKey().getTime());
                                calendar.set(HOUR_OF_DAY, parseInt(name.substring(0, 2)));
                                calendar.set(MINUTE, parseInt(name.substring(3, 5)));
                                calendar.set(SECOND, parseInt(name.substring(6, 8)));
                                timeMap.put(calendar, tp);
                            }
                        } catch (IOException x) {
                            errors.add(x);
                        }
                        for (final Path tp : timeMap.subMap(f, fromInc, t, toInc).values()) {
                            try {
                                fs.provider().delete(tp);
                                n++;
                            } catch (IOException x) {
                                errors.add(x);
                            }
                        }
                    }
                } catch (Exception x) {
                    errors.add(x);
                }
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
                try (final DirectoryStream<Path> ds = newDirectoryStream(tagPath, dateGlob)) {
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
                        final FileSystem fs = fileSystem(dp);
                        synchronized (fs) {
                            final Path d = fs.getRootDirectories().iterator().next();
                            try (final DirectoryStream<Path> s = newDirectoryStream(d)) {
                                for (final Path tp : s) {
                                    final String tpn = tp.getFileName().toString();
                                    if (parseInt(tpn.substring(0, 2)) != c.get(HOUR_OF_DAY)) {
                                        continue;
                                    }
                                    if (parseInt(tpn.substring(3, 5)) != c.get(MINUTE)) {
                                        continue;
                                    }
                                    if (parseInt(tpn.substring(6, 8)) != c.get(SECOND)) {
                                        continue;
                                    }
                                    Files.delete(tp);
                                    n++;
                                }
                            }
                        }
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
    public SafeResult<Set<Class<?>>> supportedTypes() {
        return new SimpleSafeResult<>(supportedTypes, Collections.<Throwable>emptyList());
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
        dir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public void close() throws Exception {
        watchService.close();
    }
}
