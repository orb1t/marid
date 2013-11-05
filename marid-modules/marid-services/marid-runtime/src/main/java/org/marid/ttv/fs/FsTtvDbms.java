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

package org.marid.ttv.fs;

import org.marid.ttv.TagTimeValueDbms;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import static com.google.common.base.StandardSystemProperty.USER_HOME;
import static java.nio.file.StandardWatchEventKinds.*;
import static java.util.Collections.unmodifiableSet;
import static org.marid.methods.LogMethods.warning;
import static org.marid.methods.PropMethods.get;

/**
 * @author Dmitry Ovchinnikov
 */
public class FsTtvDbms implements TagTimeValueDbms, FileVisitor<Path>, Runnable {

    private static final Logger LOG = Logger.getLogger(FsTtvDbms.class.getName());

    private final Path dir;
    private final Set<String> tagSet = new ConcurrentSkipListSet<>();
    private final AtomicLong usedSize = new AtomicLong();
    private final Map<? extends Class<?>, TypeHandler> typeBinding = new IdentityHashMap<>();
    private final Set<Class<?>> supportedTypes = unmodifiableSet(typeBinding.keySet());
    private final WatchService watchService;
    private volatile int maximumFetchSize;
    private volatile int queryTimeout;

    public FsTtvDbms(Map params) throws IOException {
        final Path defaultPath = Paths.get(USER_HOME.value(), "marid", "data");
        dir = get(params, Path.class, "dir", defaultPath).toAbsolutePath();
        watchService = FileSystems.getDefault().newWatchService();
        if (Files.notExists(dir)) {
            Files.createDirectories(dir);
        }
        Files.walkFileTree(dir, this);
    }

    @Override
    public void start() {
        new Thread(this).start();
    }

    @Override
    public void run() {

    }

    @Override
    public Set<String> tagSet() {
        return tagSet;
    }

    @Override
    public Date getMinTimestamp(String tag) {
        return null;
    }

    @Override
    public Date getMaxTimestamp(String tag) {
        return null;
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
    public <T> Map<String, Map<Date, T>> after(Class<T> type, Set<String> tags, Date from, boolean inc) {
        return null;
    }

    @Override
    public <T> Map<String, Map<Date, T>> before(Class<T> type, Set<String> tags, Date to, boolean inc) {
        return null;
    }

    @Override
    public <T> Map<String, Map<Date, T>> between(Class<T> type, Set<String> tags, Date from, boolean fromInc, Date to, boolean toInc) {
        return null;
    }

    @Override
    public long usedSize() {
        return usedSize.get();
    }

    @Override
    public <T> int insert(Class<T> type, Map<String, Map<Date, T>> data) {
        return 0;
    }

    @Override
    public <T> int insertOrUpdate(Class<T> type, Map<String, Map<Date, T>> data) {
        return 0;
    }

    @Override
    public <T> int update(Class<T> type, Map<String, Map<Date, T>> data) {
        return 0;
    }

    @Override
    public int remove(Class<?> type, Set<String> tags) {
        return 0;
    }

    @Override
    public int removeAfter(Class<?> type, Set<String> tags, Date from, boolean inc) {
        return 0;
    }

    @Override
    public int removeBefore(Class<?> type, Set<String> tags, Date to, boolean inc) {
        return 0;
    }

    @Override
    public int remove(Class<?> type, Set<String> tags, Date from, boolean fromInc, Date to, boolean toInc) {
        return 0;
    }

    @Override
    public int remove(Class<?> type, Map<String, Date> keys) {
        return 0;
    }

    @Override
    public void clear() {
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

    protected abstract class TypeHandler {

    }
}
