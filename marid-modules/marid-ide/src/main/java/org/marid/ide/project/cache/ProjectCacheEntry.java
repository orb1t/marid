/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
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

package org.marid.ide.project.cache;

import org.marid.ide.project.ProjectProfile;
import org.marid.logging.LogSupport;
import org.marid.misc.Calls;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.Files.isDirectory;
import static java.nio.file.StandardWatchEventKinds.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class ProjectCacheEntry implements AutoCloseable, LogSupport {

    private final ProjectProfile profile;
    private final Logger logger;
    private final FileSystem fileSystem;
    private final WatchService watchService;
    private final Map<String, Class<?>> classMap = new HashMap<>();
    private final Map<Path, WatchKey> watchKeyMap = new HashMap<>();
    private URLClassLoader classLoader;

    public ProjectCacheEntry(ProjectProfile profile) {
        this.profile = profile;
        this.logger = Logger.getLogger(profile.getName());
        try (final Stream<Path> stream = Files.walk(profile.getPath())) {
            fileSystem = profile.getPath().getFileSystem();
            watchService = fileSystem.newWatchService();
            stream
                    .filter(Files::isDirectory)
                    .filter(d -> !d.startsWith(profile.getTarget()))
                    .forEach(dir -> {
                        try {
                            final WatchKey watchKey = dir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                            log(INFO, "{0} registered for watch", dir);
                            watchKeyMap.put(dir, watchKey);
                        } catch (Exception x) {
                            log(WARNING, "Unable to register {0}", x, dir);
                        }
                    });
            classLoader = classLoader();
        } catch (Exception x) {
            throw new IllegalStateException(x);
        }
    }

    @Nonnull
    @Override
    public Logger logger() {
        return logger;
    }

    public boolean shouldBeUpdated() {
        boolean dirty = false;
        for (WatchKey watchKey = watchService.poll(); watchKey != null; watchKey = watchService.poll()) {
            if (!watchKey.isValid()) {
                watchKey.cancel();
            }
            try {
                for (final WatchEvent<?> watchEvent : watchKey.pollEvents()) {
                    final Path path = ((Path) watchKey.watchable()).resolve((Path) watchEvent.context());
                    if (path.startsWith(profile.getTarget())) {
                        continue;
                    }
                    if (watchEvent.kind() == ENTRY_DELETE) {
                        final WatchKey dirKey = watchKeyMap.remove(path);
                        if (dirKey != null) {
                            dirKey.cancel();
                        }
                    } else if (watchEvent.kind() == ENTRY_CREATE) {
                        if (isDirectory(path)) {
                            try {
                                final WatchKey key = path.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                                log(INFO, "{0} registered for watch", path);
                                watchKeyMap.put(path, key);
                            } catch (IOException x) {
                                throw new IllegalStateException(x);
                            }
                        }
                    }
                    dirty = true;
                }
            } finally {
                watchKey.reset();
            }
        }
        return dirty;
    }

    public URLClassLoader getClassLoader() {
        return classLoader;
    }

    public void update() throws Exception {
        classMap.clear();
        try (final URLClassLoader classLoader = this.classLoader) {
            if (classLoader != null) {
                log(INFO, "Closing a class loader");
            }
        }
        classLoader = classLoader();
    }

    public Class<?> getClass(String type) {
        return classMap.computeIfAbsent(type, t -> {
            try {
                return Class.forName(type, false, classLoader);
            } catch (Exception x) {
                return Object.class;
            }
        });
    }

    private void addJars(List<URL> urls, Path dir) {
        if (isDirectory(dir)) {
            final File[] files = dir.toFile().listFiles((d, name) -> name.endsWith(".jar"));
            urls.addAll(Stream.of(files).map(f -> Calls.call(() -> f.toURI().toURL())).collect(Collectors.toList()));
        }
    }

    private URLClassLoader classLoader() {
        final List<URL> urls = new ArrayList<>();
        addJars(urls, profile.getTarget().resolve("lib"));
        if (isDirectory(profile.getTarget().resolve("classes"))) {
            urls.add(Calls.call(() -> profile.getTarget().resolve("classes").toUri().toURL()));
        }
        return new URLClassLoader(urls.toArray(new URL[urls.size()]));
    }

    @Override
    public void close() throws Exception {
        try (final WatchService watchService = this.watchService; final URLClassLoader classLoader = this.classLoader) {
            if (watchService != null) {
                log(INFO, "Closing a watch service");
            }
            if (classLoader != null) {
                log(INFO, "Closing a class loader");
            }
        }
    }
}
