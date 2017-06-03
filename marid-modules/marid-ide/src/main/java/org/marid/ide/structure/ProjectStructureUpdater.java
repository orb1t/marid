/*
 * Copyright (c) 2017 Dmitry Ovchinnikov
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

package org.marid.ide.structure;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeItem.TreeModificationEvent;
import org.marid.ide.event.FileAddedEvent;
import org.marid.ide.event.FileChangeEvent;
import org.marid.ide.event.FileRemovedEvent;
import org.marid.ide.project.ProjectManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.nio.file.WatchEvent.Kind;
import java.util.List;
import java.util.Map;
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
@Lazy(false)
@Service
public class ProjectStructureUpdater implements Closeable {

    private static final Kind<?>[] EVENTS = {ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY};

    private final Path root;
    private final ApplicationEventPublisher eventPublisher;
    private final WatchService watchService;
    private final Map<Path, WatchKey> watchKeyMap = new ConcurrentHashMap<>();

    @Autowired
    public ProjectStructureUpdater(ProjectManager projectManager,
                                   ApplicationEventPublisher eventPublisher) throws IOException {
        this.root = projectManager.getProfilesDir();
        this.eventPublisher = eventPublisher;
        this.watchService = root.getFileSystem().newWatchService();
    }

    @PostConstruct
    public void run() {
        final Thread thread = new Thread(null, this::process, "structure-watcher", 96L * 1024L);
        thread.setUncaughtExceptionHandler((t, e) -> log(WARNING, "Uncaught exception in {0}", e, t));
        thread.start();
    }

    @EventListener
    private void onStart(ContextStartedEvent event) throws Exception {
        process(root);
    }

    private void process(Path path) throws IOException {
        try {
            if (Files.isHidden(path)) {
                return;
            }
            eventPublisher.publishEvent(new FileAddedEvent(path));
            if (Files.isDirectory(path)) {
                if (!watchKeyMap.containsKey(path)) {
                    watchKeyMap.put(path, path.register(watchService, EVENTS));
                }
                try (final Stream<Path> stream = Files.list(path)) {
                    final List<Path> paths = stream.collect(Collectors.toList());
                    for (final Path p : paths) {
                        process(p);
                    }
                }
            }
        } catch (NoSuchFileException x) {
            // ignore
        }
    }

    private void process() {
        try {
            process0();
        } catch (ClosedWatchServiceException x) {
            log(INFO, "Closed");
        } catch (IOException x) {
            throw new UncheckedIOException(x);
        } catch (InterruptedException x) {
            log(INFO, "Interrupted", x);
        }
    }

    private void process0() throws IOException, InterruptedException {
        while (!Thread.interrupted()) {
            final WatchKey key = watchService.take();
            final Path dir = (Path) key.watchable();
            if (key.isValid()) {
                try {
                    for (final WatchEvent<?> event : key.pollEvents()) {
                        final Path path = dir.resolve((Path) event.context());
                        if (event.kind() == ENTRY_CREATE) {
                            process(path);
                        } else if (event.kind() == ENTRY_DELETE) {
                            eventPublisher.publishEvent(new FileRemovedEvent(path));
                            watchKeyMap.entrySet().removeIf(e -> {
                                final boolean result = e.getKey().startsWith(path);
                                if (result) {
                                    e.getValue().cancel();
                                }
                                return result;
                            });
                        } else if (event.kind() == ENTRY_MODIFY) {
                            eventPublisher.publishEvent(new FileChangeEvent(path));
                        }
                    }
                } finally {
                    key.reset();
                }
            } else {
                key.cancel();
            }
        }
    }

    @Override
    public void close() throws IOException {
        watchService.close();
    }
}
