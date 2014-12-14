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

package org.marid.web;

import org.marid.service.AbstractMaridService;
import org.marid.web.WebServerParameters.Dir;
import org.marid.web.WebServerParameters.VHost;

import javax.activation.FileTypeMap;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

import static java.nio.file.StandardWatchEventKinds.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static org.marid.util.StringUtils.substitute;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class AbstractWebServer extends AbstractMaridService {

    protected static final Kind<?>[] ALL_KINDS = {ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY};

    protected final Map<String, Path> dirMap;
    protected final Map<String, Pattern> vhostPatternMap;
    protected final List<String> defaultPages;
    protected final FileTypeMap fileTypeMap = FileTypeMap.getDefaultFileTypeMap();

    public AbstractWebServer(WebServerConfig conf) {
        super(conf);
        dirMap = Arrays.stream(conf.dirs()).collect(toMap(Dir::name, d -> Paths.get(substitute(d.dir()))));
        vhostPatternMap = Arrays.stream(conf.vHosts()).collect(toMap(VHost::name, v -> Pattern.compile(v.pattern())));
        defaultPages = new ArrayList<>(Arrays.asList(conf.defaultPages()));
    }

    @Override
    public void start() throws Exception {
        super.start();
        for (final Path dir : dirMap.values()) {
            try {
                newThread(new DirectoryWatcher(dir)).start();
            } catch (IOException x) {
                warning("{0} Unable to create watcher for {1}", x, this, dir);
            }
        }
    }

    protected boolean filterPath(Path path) {
        return path.getFileName().toString().startsWith("_") || watchFilter(path);
    }

    protected boolean watchFilter(Path path) {
        final String f = path.getFileName().toString();
        return f.startsWith(".") || f.endsWith(".bak") || f.endsWith(".new");
    }

    protected String toContextPath(Path path) {
        return StreamSupport.stream(path.spliterator(), false).map(Path::toString).collect(joining("/", "/", ""));
    }

    protected abstract void onAdd(Path dir, Path context);

    protected abstract void onModify(Path dir, Path context);

    protected abstract void onDelete(Path dir, Path context);

    protected class DirectoryWatcher implements Runnable, FileVisitor<Path> {

        protected final Path dir;
        protected final WatchService watchService;

        public DirectoryWatcher(Path dir) throws IOException {
            this.dir = dir;
            this.watchService = watchService(dir);
            Files.walkFileTree(dir, this);
        }

        private WatchService watchService(Path dir) {
            try {
                return dir.getFileSystem().newWatchService();
            } catch (Exception x) {
                return null;
            }
        }

        @Override
        public void run() {
            try {
                while (watchService != null && isRunning()) {
                    final WatchKey key = watchService.poll(timeGranularity, MILLISECONDS);
                    if (key == null) {
                        continue;
                    }
                    try {
                        for (final WatchEvent<?> event : key.pollEvents()) {
                            final Path path = (Path) event.context();
                            if (watchFilter(path)) {
                                continue;
                            }
                            if (event.kind() == ENTRY_CREATE) {
                                onAdd(dir, path);
                            } else if (event.kind() == ENTRY_DELETE) {
                                onDelete(dir, path);
                            } else if (event.kind() == ENTRY_MODIFY) {
                                onModify(dir, path);
                            } else {
                                throw new IllegalStateException("Kind: " + event.kind());
                            }
                            info("{0} {1} {2}", AbstractWebServer.this, event.kind(), dir);
                        }
                    } finally {
                        key.reset();
                    }
                }
            } catch (InterruptedException x) {
                warning("{0} Watcher for {1} was terminated", x, AbstractWebServer.this, dir);
            } catch (Exception x) {
                severe("{0} Watcher for {1} error", x, AbstractWebServer.this, dir);
            }
        }

        @Override
        public FileVisitResult preVisitDirectory(Path d, BasicFileAttributes a) throws IOException {
            if (filterPath(d)) {
                finest("{0} Skipped {1}", AbstractWebServer.this, d);
                return FileVisitResult.SKIP_SUBTREE;
            } else {
                if (watchService != null) {
                    d.register(watchService, ALL_KINDS);
                }
                fine("{0} Registered {1}", AbstractWebServer.this, d);
                return FileVisitResult.CONTINUE;
            }
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes a) throws IOException {
            if (filterPath(file)) {
                finest("{0} Filtered {1}", AbstractWebServer.this, file);
            } else {
                try {
                    onAdd(dir, dir.relativize(file));
                } catch (Exception x) {
                    warning("{0} Unable to add {1}", AbstractWebServer.this, file);
                }
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            warning("{0} Failed visit file {1}", exc, AbstractWebServer.this, file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            if (exc != null) {
                warning("{0} Failed visit directory {1}", exc, AbstractWebServer.this, dir);
            }
            return FileVisitResult.CONTINUE;
        }
    }
}
