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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.marid.dyn.TypeCaster;
import org.marid.groovy.GroovyRuntime;
import org.marid.service.AbstractMaridService;

import javax.activation.FileTypeMap;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import static java.nio.file.StandardWatchEventKinds.*;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.marid.dyn.TypeCaster.TYPE_CASTER;
import static org.marid.methods.LogMethods.*;
import static org.marid.methods.PropMethods.*;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class AbstractWebServer extends AbstractMaridService {

    protected static final Kind<?>[] ALL_KINDS = {ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY};

    protected final Map<String, Path> dirMap;
    protected final Map<String, Pattern> vhostPatternMap;
    protected final BiMap<String, String> vhostMap;
    protected final List<String> defaultPages;
    protected final FileTypeMap fileTypeMap = FileTypeMap.getDefaultFileTypeMap();

    public AbstractWebServer(Map params) {
        super(params);
        dirMap = dirMap(get(params, Map.class, "dirMap", defaultDirMap()));
        vhostPatternMap = vhostPatternMap(get(params, Map.class, "vhostPatternMap", emptyMap()));
        vhostMap = vhostMap(get(params, Map.class, "vhostMap", emptyMap()));
        defaultPages = defaultPages(get(params, Collection.class, "defaultPages", emptyList()));
    }

    protected Map<String, Path> defaultDirMap() {
        return singletonMap("default", Paths.get(System.getProperty("user.home"), "marid", "web"));
    }

    protected Map<String, Path> dirMap(Map map) {
        final Map<String, Path> dmap = new HashMap<>();
        for (final Object oe : map.entrySet()) {
            final Entry e = (Entry) oe;
            try {
                if (e.getValue() instanceof String) {
                    dmap.put(e.getKey().toString(), Paths.get((String) e.getValue()));
                } else if (e.getValue() instanceof File) {
                    dmap.put(e.getKey().toString(), ((File) e.getValue()).toPath());
                } else if (e.getValue() instanceof URL) {
                    dmap.put(e.getKey().toString(), Paths.get(((URL) e.getValue()).toURI()));
                } else if (e.getValue() instanceof URI) {
                    dmap.put(e.getKey().toString(), Paths.get((URI) e.getValue()));
                } else {
                    dmap.put(e.getKey().toString(), TYPE_CASTER.cast(Path.class, e.getValue()));
                }
            } catch (Exception x) {
                warning(log, "{0} Unable to add {1}", x, this, oe);
            }
        }
        return dmap;
    }

    protected Map<String, Pattern> vhostPatternMap(Map map) {
        if (map.isEmpty()) {
            return emptyMap();
        } else {
            final Map<String, Pattern> vpm = new HashMap<>();
            for (final Object oe : map.entrySet()) {
                final Entry e = (Entry) oe;
                try {
                    if (e.getValue() instanceof String) {
                        vpm.put(e.getKey().toString(), Pattern.compile((String) e.getValue()));
                    } else {
                        vpm.put(e.getKey().toString(), TYPE_CASTER.cast(Pattern.class, e.getValue()));
                    }
                } catch (Exception x) {
                    warning(log, "{0} Unable to add vhost pattern {1}", x, this, oe);
                }
            }
            return vpm;
        }
    }

    protected BiMap<String, String> vhostMap(Map map) {
        if (map.isEmpty()) {
            return ImmutableBiMap.of();
        } else {
            final BiMap<String, String> bimap = HashBiMap.create(map.size());
            for (final Object oe : map.entrySet()) {
                final Entry e = (Entry) oe;
                try {
                    bimap.put(e.getKey().toString(), TYPE_CASTER.cast(String.class, e.getValue()));
                } catch (Exception x) {
                    warning(log, "{0} Unable to add vhost entry {1}", x, this, oe);
                }
            }
            return bimap;
        }
    }
    
    protected List<String> defaultPages(Collection collection) {
        if (collection.isEmpty()) {
            return emptyList();
        } else {
            final List<String> list = new ArrayList<>(collection.size());
            for (final Object o : collection) {
                list.add(TYPE_CASTER.cast(String.class, o));
            }
            return list;
        }
    }

    @Override
    protected void onRunning() {
        super.onRunning();
        for (final Path dir : dirMap.values()) {
            try {
                newThread(new DirectoryWatcher(dir)).start();
            } catch (IOException x) {
                warning(log, "{0} Unable to create watcher for {1}", x, this, dir);
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

    @SuppressWarnings("unchecked")
    protected String toContextPath(Path path) {
        return "/" + DefaultGroovyMethods.join((Iterator)path.iterator(), "/");
    }

    protected abstract void onAdd(Path dir, Path context);

    protected abstract void onModify(Path dir, Path context);

    protected abstract void onDelete(Path dir, Path context);

    protected class DirectoryWatcher implements FileVisitor<Path>, Runnable {

        protected final Path dir;
        protected final WatchService watchService;

        public DirectoryWatcher(Path dir) throws IOException {
            this.dir = dir;
            this.watchService = FileSystems.getDefault().newWatchService();
            Files.walkFileTree(dir, this);
        }

        @Override
        public void run() {
            try {
                while (isRunning()) {
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
                            info(log, "{0} {1} {2}", AbstractWebServer.this, event.kind(), dir);
                        }
                    } finally {
                        key.reset();
                    }
                }
            } catch (InterruptedException x) {
                warning(log, "{0} Watcher for {1} was terminated", x, AbstractWebServer.this, dir);
            } catch (Exception x) {
                severe(log, "{0} Watcher for {1} error", x, AbstractWebServer.this, dir);
            }
        }

        @Override
        public FileVisitResult preVisitDirectory(Path d, BasicFileAttributes a) throws IOException {
            if (filterPath(d)) {
                finest(log, "{0} Skipped {1}", AbstractWebServer.this, d);
                return FileVisitResult.SKIP_SUBTREE;
            } else {
                d.register(watchService, ALL_KINDS);
                fine(log, "{0} Registered {1}", AbstractWebServer.this, d);
                return FileVisitResult.CONTINUE;
            }
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes a) throws IOException {
            if (filterPath(file)) {
                finest(log, "{0} Filtered {1}", AbstractWebServer.this, file);
            } else {
                try {
                    onAdd(dir, dir.relativize(file));
                    info(log, "{0} Added {1}", AbstractWebServer.this, file);
                } catch (Exception x) {
                    warning(log, "{0} Unable to add {1}", AbstractWebServer.this, file);
                }
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            warning(log, "{0} Failed visit file {1}", exc, AbstractWebServer.this, file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            if (exc != null) {
                warning(log, "{0} Failed visit directory {1}", exc, AbstractWebServer.this, dir);
            }
            return FileVisitResult.CONTINUE;
        }
    }
}
