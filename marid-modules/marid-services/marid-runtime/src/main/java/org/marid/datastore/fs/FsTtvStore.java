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
import org.marid.datastore.TtvStore;
import org.marid.service.AbstractMaridService;

import java.io.FileNotFoundException;
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
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.*;
import static java.nio.file.StandardWatchEventKinds.*;
import static java.util.Collections.singletonMap;
import static java.util.Collections.unmodifiableSet;
import static org.marid.methods.LogMethods.fine;
import static org.marid.methods.LogMethods.warning;
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
    private final String fileSystemScheme;
    private final FileSystemProvider fileSystemProvider;
    private final Cache<Path, FileSystem> fileSystemCache;
    private final String dateGlob;
    private volatile int maximumFetchSize;
    private volatile int queryTimeout;

    @SuppressWarnings("unchecked")
    public FsTtvStore(Map params) throws IOException {
        super(params);
        final Path defaultPath = Paths.get(USER_HOME.value(), "marid", "data");
        fileSystemScheme = get(params, String.class, "fileSystemScheme", "zip");
        dateGlob = "????-??-??." + fileSystemScheme;
        FileSystemProvider fsProvider = null;
        for (final FileSystemProvider provider : FileSystemProvider.installedProviders()) {
            if (Objects.equals(provider.getScheme(), fileSystemScheme)) {
                fsProvider = provider;
                break;
            }
        }
        if (fsProvider == null) {
            throw new NoSuchElementException("No such file system provider: " + fileSystemScheme);
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
                    public void onRemoval(RemovalNotification<Path, FileSystem> notification) {
                        try {
                            final FileSystem fs = notification.getValue();
                            if (fs != null) {
                                fs.close();
                                fine(LOG, "Remove {0} from cache", notification.getKey());
                            }
                        } catch (Exception x) {
                            warning(LOG, "Unable to close {0}", x, notification.getKey());
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
    public Set<String> tagSet(final String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return tagSet;
        } else {
            return Sets.filter(tagSet, new Predicate<String>() {
                @Override
                public boolean apply(String input) {
                    return input.matches(pattern);
                }
            });
        }
    }

    private Map<String, Date> getMaxMinTimestamp(Class<?> type, Set<String> tags, boolean min) {
        final Map<String, Date> map = new HashMap<>();
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
    public Map<String, Date> getMinTimestamp(Class<?> type, Set<String> tags) {
        return getMaxMinTimestamp(type, tags, true);
    }

    @Override
    public Map<String, Date> getMaxTimestamp(Class<?> type, Set<String> tags) {
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
    public <T> Map<String, NavigableMap<Date, T>> after(Class<T> type, Set<String> tags, Date from, boolean inc) {
        return null;
    }

    @Override
    public <T> Map<String, NavigableMap<Date, T>> before(Class<T> type, Set<String> tags, Date to, boolean inc) {
        return null;
    }

    @Override
    public <T> Map<String, NavigableMap<Date, T>> between(Class<T> type, Set<String> tags, Date from, boolean fromInc, Date to, boolean toInc) {
        return null;
    }

    @Override
    public long usedSize() {
        return usedSize.get();
    }

    private FileSystem fileSystem(final Path path) throws Exception {
        return fileSystemCache.get(path, new Callable<FileSystem>() {
            @Override
            public FileSystem call() throws Exception {
                return fileSystemProvider.newFileSystem(path, singletonMap("create", true));
            }
        });
    }

    private String dateFile(Calendar calendar) {
        return String.format("%04d-%02d-%02d.%s",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DATE),
                fileSystemScheme);
    }

    private String timeFile(Calendar calendar, String ext) {
        return String.format("%02d-%02d-%02d.%s",
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND),
                ext);
    }

    @SuppressWarnings("unchecked")
    public <T> long insert(Class<T> t, Map<String, Map<Date, T>> data, Set<? extends OpenOption> o) {
        final TypeHandler<T> th = (TypeHandler<T>) typeBinding.get(t);
        final String ext;
        try {
            ext = th.getExtension();
        } catch (NullPointerException x) {
            throw new IllegalArgumentException("Unsupported type: " + t, x);
        }
        final GregorianCalendar calendar = new GregorianCalendar();
        long n = 0;
        for (final Entry<String, Map<Date, T>> te : data.entrySet()) {
            final Path tagPath = dir.resolve(te.getKey());
            try {
                Files.createDirectories(tagPath);
            } catch (IOException x) {
                throw new IllegalStateException(x);
            }
            for (final Entry<Date, T> e : te.getValue().entrySet()) {
                try {
                    calendar.setTime(e.getKey());
                    final Path datePath = tagPath.resolve(dateFile(calendar));
                    final FileSystem fs = fileSystem(datePath);
                    final Path timePath = fs.getPath(timeFile(calendar, ext));
                    try (final FileChannel ch = fileSystemProvider.newFileChannel(timePath, o)) {
                        ch.write(UTF_8.encode(th.toString(e.getValue())));
                    }
                    n++;
                } catch (FileAlreadyExistsException x) {
                    warning(LOG, "{0} already exists", x.getFile());
                } catch (FileNotFoundException x) {
                    warning(LOG, x.getMessage());
                } catch (Exception x) {
                    throw new IllegalStateException(x);
                }
            }
        }
        return n;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> long insert(Class<T> type, Map<String, Map<Date, T>> data) {
        return insert(type, data, EnumSet.of(CREATE_NEW, WRITE));
    }

    @Override
    public <T> long insertOrUpdate(Class<T> type, Map<String, Map<Date, T>> data) {
        return insert(type, data, EnumSet.of(CREATE, WRITE, TRUNCATE_EXISTING));
    }

    @Override
    public <T> long update(Class<T> type, Map<String, Map<Date, T>> data) {
        return insert(type, data, EnumSet.of(WRITE, TRUNCATE_EXISTING));
    }

    @Override
    public long remove(Class<?> type, Set<String> tags) {
        final AtomicLong counter = new AtomicLong();
        for (final String tag : tags) {
            final Path tagPath = dir.resolve(tag);
            try {
                try (final DirectoryStream<Path> ds = Files.newDirectoryStream(tagPath, dateGlob)) {
                    for (final Path dp : ds) {
                        try (final DirectoryStream<Path> ts = Files.newDirectoryStream(dp, "??-??-??.*")) {
                            for (final Path tp : ts) {
                                try {
                                    if (Files.deleteIfExists(tp)) {
                                        counter.incrementAndGet();
                                    }
                                } catch (IOException x) {
                                    warning(LOG, "Unable to delete {0}", x, tp);
                                }
                            }
                        }
                        try {
                            Files.deleteIfExists(dp);
                        } catch (DirectoryNotEmptyException x) {
                            fine(LOG, "Directory {0} is not empty", x.getFile());
                        }
                    }
                }
                try {
                    Files.deleteIfExists(tagPath);
                } catch (DirectoryNotEmptyException x) {
                    fine(LOG, "Directory {0} is not empty", x.getFile());
                }
            } catch (IOException x) {
                throw new IllegalStateException(x);
            }
        }
        return counter.get();
    }

    @Override
    public long removeAfter(Class<?> type, Set<String> tags, Date from, boolean inc) {
        return 0;
    }

    @Override
    public long removeBefore(Class<?> type, Set<String> tags, Date to, boolean inc) {
        return 0;
    }

    @Override
    public long removeBetween(Class<?> type, Set<String> tags, Date from, boolean fromInc, Date to, boolean toInc) {
        return 0;
    }

    @Override
    public long removeKeys(Class<?> type, Map<String, Date> keys) {
        return 0;
    }

    @Override
    public long clear() {
        return remove(Object.class, tagSet);
    }

    @Override
    public Set<Class<?>> supportedTypes() {
        return supportedTypes;
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
