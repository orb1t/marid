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

package org.marid.dependant.resources;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.marid.ide.project.ProjectProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.StandardWatchEventKinds.*;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class ResourcesTracker implements Closeable {

    public final ObservableList<Path> resources = FXCollections.observableArrayList();

    private final Path resourcesPath;
    private final WatchService watchService;
    private final Map<Path, WatchKey> watchKeyMap = new ConcurrentHashMap<>();

    @Autowired
    public ResourcesTracker(ProjectProfile profile) throws IOException {
        resourcesPath = profile.getSrcMainResources();
        watchService = resourcesPath.getFileSystem().newWatchService();
    }

    @PostConstruct
    private void start() throws IOException {
        final WatchEvent.Kind<?>[] kinds = {ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY};
        final Set<Path> directories;
        try (final Stream<Path> files = Files.walk(resourcesPath)) {
            directories = files.filter(Files::isDirectory).collect(Collectors.toSet());
        }
        for (final Path directory : directories) {
            directory.register(watchService, kinds);
        }
        try (final Stream<Path> files = Files.walk(resourcesPath)) {
            files.filter(Files::isRegularFile).forEach(resources::add);
        }
        final Thread thread = new Thread(null, () -> {
            while (true) {
                final WatchKey watchKey;
                try {
                    watchKey = watchService.take();
                } catch (ClosedWatchServiceException | InterruptedException exception) {
                    break;
                }
                if (!watchKey.isValid()) {
                    watchKey.cancel();
                    continue;
                }
                for (final WatchEvent<?> event : watchKey.pollEvents()) {
                    final Path path = ((Path) watchKey.watchable()).resolve((Path) event.context());
                    final WatchEvent.Kind<?> kind = event.kind();
                    if (kind.equals(ENTRY_DELETE)) {
                        Platform.runLater(() -> resources.remove(path));
                        final WatchKey wk = watchKeyMap.remove(path);
                        if (wk != null) {
                            wk.cancel();
                        }
                    } else if (kind.equals(ENTRY_CREATE)) {
                        if (Files.isRegularFile(path)) {
                            Platform.runLater(() -> {
                                final int index = resources.indexOf(path);
                                if (index < 0) {
                                    resources.add(path);
                                } else {
                                    final Path p = resources.remove(index);
                                    resources.add(index, p);
                                }
                            });
                        } else if (Files.isDirectory(path)) {
                            try {
                                final WatchKey old = watchKeyMap.put(path, path.register(watchService, kinds));
                                if (old != null) {
                                    old.cancel();
                                }
                            } catch (IOException ioException) {
                                log(WARNING, "Unable to register {0}", ioException, path);
                            }
                        }
                    } else if (kind.equals(ENTRY_MODIFY)) {
                        Platform.runLater(() -> {
                            final int index = resources.indexOf(path);
                            if (index >= 0) {
                                final Path p = resources.remove(index);
                                resources.add(index, p);
                            }
                        });
                    }
                }
                watchKey.reset();
            }
            log(INFO, "Terminated");
        }, resourcesPath.toString(), 96L * 1024L);
        thread.start();
    }

    public Path getResourcesPath() {
        return resourcesPath;
    }

    public Path resolve(String path) {
        return resourcesPath.resolve(path);
    }

    @Override
    public void close() throws IOException {
        watchService.close();
    }
}
