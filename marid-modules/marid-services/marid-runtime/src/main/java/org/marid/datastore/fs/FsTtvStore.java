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
import groovy.json.JsonSlurper;
import org.marid.datastore.TtvStore;
import org.marid.io.SafeResult;
import org.marid.io.SimpleSafeResult;
import org.marid.service.AbstractMaridService;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Timestamp;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import static com.google.common.base.StandardSystemProperty.USER_HOME;
import static java.lang.Integer.parseInt;
import static java.nio.channels.Channels.newReader;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.newDirectoryStream;
import static java.nio.file.StandardOpenOption.*;
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
public class FsTtvStore extends AbstractMaridService implements TtvStore, FileVisitor<Path> {

    private static final Logger LOG = Logger.getLogger(FsTtvStore.class.getName());
    private static final Set<? extends OpenOption> FOPTS = EnumSet.of(CREATE, WRITE, READ);

    private final Path dir;
    private final Set<String> tagSet = new ConcurrentSkipListSet<>();
    private final AtomicLong usedSize = new AtomicLong();
    private final WatchService watchService;
    private final Cache<Path, DateEntry> entryCache;
    private final int bufferSize;
    private volatile int maximumFetchSize;
    private volatile int queryTimeout;

    @SuppressWarnings("unchecked")
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
                .removalListener(new RemovalListener<Path, DateEntry>() {
                    @Override
                    public void onRemoval(RemovalNotification<Path, DateEntry> e) {
                        try {
                            final DateEntry de = e.getValue();
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
                } catch (Exception x) {
                    warning(LOG, "Watch service error", x);
                }
            }
        }).start();

    }

    @Override
    protected void doStop() {
        entryCache.invalidateAll();
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

    private SafeResult<Map<String, Date>> getMaxMinTimestamp(Class<?> type, Set<String> tags, boolean min) {
        final List<Throwable> errors = new LinkedList<>();
        final Map<String, Date> map = new TreeMap<>();
        TAG_LOOP:
        for (final String tag : tags) {
            final Path tagPath = dir.resolve(tag);
            if (Files.isDirectory(tagPath)) {
                try {
                    final TreeSet<Path> datePaths = listSorted(tagPath, "????-??-??.data");
                    for (final Path datePath : min ? datePaths : datePaths.descendingSet()) {
                        try {
                            final DateEntry de = dateEntry(datePath);
                            synchronized (de) {
                                if (!de.records.isEmpty()) {
                                    map.put(tag, min ? de.records.firstKey() : de.records.lastKey());
                                    continue TAG_LOOP;
                                }
                            }
                        } catch (Exception x) {
                            errors.add(x);
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
            Class<T> type, Set<String> tags, Date from, boolean fromInc, Date to, boolean toInc) {
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
                try {
                    final DateEntry de = dateEntry(dp);
                    synchronized (de) {
                        for (final Entry<Date, Record> re : de.records.subMap(from, fromInc, to, toInc).entrySet()) {
                            final String text = re.getValue().text;
                            try {
                                final Object v;
                                if ("null".equals(text)) {
                                    v = null;
                                } else if (type == Double.class || type == double.class) {
                                    v = Double.valueOf(text);
                                } else if (type == Float.class || type == float.class) {
                                    v = Float.valueOf(text);
                                } else if (type == Long.class || type == long.class) {
                                    v = Long.valueOf(text);
                                } else if (type == Integer.class || type == int.class) {
                                    v = Integer.valueOf(text);
                                } else if (type == Boolean.class || type == boolean.class) {
                                    v = Boolean.valueOf(text);
                                } else if (type == Short.class || type == short.class) {
                                    v = Short.valueOf(text);
                                } else if (type == Byte.class || type == byte.class) {
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
                } catch (Exception x) {
                    errors.add(x);
                    warning(LOG, "Unable to process {0}", x, dp);
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

    private DateEntry dateEntry(final Path path) throws Exception {
        return entryCache.get(path, new Callable<DateEntry>() {
            @Override
            public DateEntry call() throws Exception {
                return new DateEntry(path);
            }
        });
    }

    private String dateFile(Calendar c) {
        return String.format("%04d-%02d-%02d.data", c.get(YEAR), c.get(MONTH) + 1, c.get(DATE));
    }

    public <T> SafeResult<Long> insert(Class<T> t, Map<String, Map<Date, T>> data, boolean insert, boolean update) {
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
                    final DateEntry de = dateEntry(dp);
                    final T value = cast(t, e.getValue());
                    final String v;
                    if (value instanceof Number) {
                        v = JsonOutput.toJson((Number) value);
                    } else if (value instanceof String) {
                        v = JsonOutput.toJson((String) value);
                    } else if (value instanceof Boolean) {
                        v = JsonOutput.toJson((Boolean) value);
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
                    synchronized (de) {
                        de.put(e.getKey(), v, insert, update);
                        n++;
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
                    try {
                        final DateEntry de = dateEntry(dp);
                        synchronized (de) {
                            de.ch.truncate(0L);
                        }
                        entryCache.invalidate(dp);
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
                final Path dp = e.getValue();
                try {
                    final DateEntry de = dateEntry(dp);
                    synchronized (de) {
                        de.remove(from, fromInc, to, toInc);
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
                        try {
                            final DateEntry de = dateEntry(dp);
                            synchronized (de) {
                                de.remove(e.getValue());
                            }
                        } catch (Exception x) {
                            errors.add(x);
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

    @Override
    public void close() throws Exception {
        watchService.close();
    }

    class DateEntry implements Closeable {

        final Path file;
        final FileChannel ch;
        final TreeMap<Date, Record> records = new TreeMap<>();
        final Calendar calendar;
        final ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize);
        final CharsetDecoder decoder = UTF_8.newDecoder();
        final CharsetEncoder encoder = UTF_8.newEncoder();
        final JsonSlurper slurper = new JsonSlurper();

        DateEntry(Path path) throws IOException {
            file = path;
            ch = FileChannel.open(file, FOPTS);
            final String f = file.getFileName().toString();
            calendar = new GregorianCalendar(
                    parseInt(f.substring(0, 4)),
                    parseInt(f.substring(5, 7)) - 1,
                    parseInt(f.substring(8, 10)));
            final BufferedReader r = new BufferedReader(newReader(ch, decoder, bufferSize));
            long pos = 0L;
            while (true) {
                final String line = r.readLine();
                if (line == null) {
                    break;
                }
                final int maxCount = (int) (line.length() * encoder.maxBytesPerChar() * 2);
                final int len;
                if (maxCount <= bufferSize) {
                    buffer.clear();
                    final CoderResult cr = encoder.encode(CharBuffer.wrap(line), buffer, true);
                    if (cr.isError()) {
                        cr.throwException();
                    }
                    len = buffer.position();
                } else {
                    final ByteBuffer buf = encoder.encode(CharBuffer.wrap(line));
                    len = buf.limit();
                }
                calendar.set(HOUR_OF_DAY, parseInt(line.substring(0, 2)));
                calendar.set(MINUTE, parseInt(line.substring(3, 5)));
                calendar.set(SECOND, parseInt(line.substring(6, 8)));
                records.put(calendar.getTime(), new Record(pos, len, line.substring(9)));
                pos += len + 1;
            }
            assert ch.size() == ch.position();
        }

        void remove(Date from, boolean fromInc, Date to, boolean toInc) throws IOException {
            for (final Date date : records.subMap(from, fromInc, to, toInc).keySet()) {
                remove(date);
            }
        }

        boolean remove(Date date) throws IOException {
            final Record r = records.remove(date);
            if (r == null) {
                return false;
            }
            ch.position(r.position);
            long pos = r.position + r.length + 1;
            final long size = ch.size();
            while (pos < size) {
                pos += ch.transferTo(pos, size - pos, ch);
            }
            ch.truncate(size - r.length - 1);
            ch.position(ch.size());
            return true;
        }

        void put(Date date, String value, boolean insert, boolean update) throws IOException {
            if (insert) {
                if (update) {
                    remove(date);
                }
            } else {
                if (!remove(date)) {
                    throw new IllegalStateException(toString(date) + " does not exist");
                }
            }
            calendar.setTime(date);
            final StringBuilder builder = new StringBuilder(10 + value.length());
            {
                final int hour = calendar.get(HOUR_OF_DAY);
                if (hour < 10) {
                    builder.append('0');
                }
                builder.append(hour);
            }
            builder.append('-');
            {
                final int minute = calendar.get(MINUTE);
                if (minute < 10) {
                    builder.append('0');
                }
                builder.append(minute);
            }
            builder.append('-');
            {
                final int second = calendar.get(SECOND);
                if (second < 10) {
                    builder.append('0');
                }
                builder.append(second);
            }
            builder.append('\t').append(value).append('\n');
            final long position = ch.position();
            final CharBuffer charBuffer = CharBuffer.wrap(builder);
            int length = 0;
            while (true) {
                buffer.clear();
                final CoderResult cr = encoder.encode(charBuffer, buffer, true);
                if (cr.isOverflow()) {
                    length += buffer.position();
                    buffer.flip();
                    ch.write(buffer);
                    continue;
                }
                if (cr.isUnderflow()) {
                    length += buffer.position();
                    buffer.flip();
                    ch.write(buffer);
                    break;
                }
                if (cr.isError()) {
                    cr.throwException();
                }
            }
            final Record old = records.put(date, new Record(position, length - 1, value));
            if (old != null) {
                throw new IllegalStateException(toString(date) + " already exists");
            }
        }

        @Override
        public void close() throws IOException {
            ch.close();
        }

        @Override
        public String toString() {
            return "Entry " + file;
        }

        String toString(Date date) {
            return toString() + " " + new Timestamp(date.getTime());
        }
    }

    static class Record {

        final long position;
        final int length;
        final String text;

        Record(long position, int length, String text) {
            this.position = position;
            this.length = length;
            this.text = text;
        }
    }
}
