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

package org.marid.ide.panes.structure;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeItem.TreeModificationEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.nio.file.WatchEvent.Kind;
import java.util.Collections;
import java.util.Comparator;
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
public class ProjectStructureTreeUpdater implements Closeable {

    private static final Kind<?>[] EVENTS = {ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY};

    private final Path root;
    private final WatchService watchService;
    private final ProjectStructureTree tree;
    private final Map<Path, WatchKey> watchKeyMap = new ConcurrentHashMap<>();
    private final Map<Path, Boolean> expandedState = new ConcurrentHashMap<>();

    @Autowired
    public ProjectStructureTreeUpdater(ProjectStructureTree tree) throws IOException {
        this.root = tree.getRoot().getValue();
        this.tree = tree;
        this.watchService = root.getFileSystem().newWatchService();
        process(root);
        Platform.runLater(() -> collapse(tree.getRoot()));
    }

    private void collapse(TreeItem<Path> item) {
        if (item.getChildren().stream().allMatch(i -> i.getChildren().isEmpty())) {
            item.setExpanded(false);
        }
        item.getChildren().forEach(this::collapse);
    }

    @PostConstruct
    public void run() {
        final Thread thread = new Thread(null, this::process, "structure-watcher", 96L * 1024L);
        thread.setUncaughtExceptionHandler((t, e) -> log(WARNING, "Uncaught exception in {0}", e, t));
        thread.start();
    }

    private void process(Path path) throws IOException {
        try {
            if (Files.isHidden(path)) {
                return;
            }
            Platform.runLater(() -> onAdd(path, tree.getRoot()));
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

    private void remove(Path dir) {
        if (dir.getParent().equals(root)) {
            expandedState.keySet().removeIf(p -> p.startsWith(root));
        }
        watchKeyMap.entrySet().removeIf(e -> {
            final boolean result = e.getKey().startsWith(dir);
            if (result) {
                e.getValue().cancel();
            }
            return result;
        });
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
                            remove(path);
                            Platform.runLater(() -> onDelete(path, tree.getRoot()));
                        } else if (event.kind() == ENTRY_MODIFY) {
                            Platform.runLater(() -> onModify(path, tree.getRoot()));
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

    private void onAdd(Path path, TreeItem<Path> item) {
        if (path.equals(item.getValue())) {
            Event.fireEvent(item, new TreeModificationEvent<>(TreeItem.valueChangedEvent(), item));
            return;
        }
        if (!path.startsWith(item.getValue())) {
            return;
        }
        if (item.getChildren().stream().map(TreeItem::getValue).anyMatch(path::startsWith)) {
            item.getChildren().forEach(e -> onAdd(path, e));
        } else {
            final TreeItem<Path> newPathItem = new TreeItem<>(path);
            newPathItem.setExpanded(expandedState.getOrDefault(path, true));
            newPathItem.expandedProperty().addListener((o, ov, nv) -> {
                if (nv) {
                    expandedState.remove(path);
                } else {
                    expandedState.put(path, false);
                }
            });
            final Comparator<TreeItem<Path>> comparator = (i1, i2) -> {
                if (Files.isDirectory(i1.getValue()) && Files.isDirectory(i2.getValue())) {
                    return i1.getValue().compareTo(i2.getValue());
                } else if (Files.isRegularFile(i1.getValue()) && Files.isRegularFile(i2.getValue())) {
                    return i1.getValue().compareTo(i2.getValue());
                } else if (Files.isDirectory(i1.getValue())) {
                    return -1;
                } else {
                    return 1;
                }
            };
            final int index = Collections.binarySearch(item.getChildren(), newPathItem, comparator);
            if (index >= 0) {
                log(WARNING, "Duplicate detected: {0}", path);
            } else {
                item.getChildren().add(-(index + 1), newPathItem);
            }
        }
    }

    private void onDelete(Path path, TreeItem<Path> item) {
        if (!item.getChildren().removeIf(i -> i.getValue().equals(path))) {
            item.getChildren().forEach(i -> onDelete(path, i));
        }
    }

    private void onModify(Path path, TreeItem<Path> item) {
        if (path.equals(item.getValue())) {
            Event.fireEvent(item, new TreeModificationEvent<>(TreeItem.valueChangedEvent(), item));
        } else {
            item.getChildren().forEach(e -> onModify(path, e));
        }
    }

    @Override
    public void close() throws IOException {
        watchService.close();
    }
}
