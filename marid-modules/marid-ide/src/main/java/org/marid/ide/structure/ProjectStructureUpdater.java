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

import org.marid.ide.event.*;
import org.marid.ide.project.ProjectManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
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
    private final ConcurrentMap<UUID, Path> fileIds = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<PropagatedEvent> eventQueue = new ConcurrentLinkedQueue<>();

    @Autowired
    public ProjectStructureUpdater(ProjectManager projectManager, ApplicationEventPublisher eventPublisher) throws IOException {
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
        onAdd(root);
    }

    private void onAdd(Path path) throws IOException {
        try {
            if (Files.isHidden(path)) {
                return;
            }
            final UUID uuid = uuid(path);
            if (uuid == null) {
                fileIds.put(uuid(path, UUID.randomUUID()), path);
            } else {
                fileIds.putIfAbsent(uuid, path);
            }
            eventQueue.add(new FileAddedEvent(path));
            if (Files.isDirectory(path)) {
                path.register(watchService, EVENTS);
                try (final Stream<Path> stream = Files.list(path)) {
                    final List<Path> paths = stream.collect(Collectors.toList());
                    for (final Path p : paths) {
                        onAdd(p);
                    }
                }
            }
        } catch (NoSuchFileException x) {
            // ignore
        }
    }

    private void onDelete(Path path) throws IOException {
        eventQueue.add(new FileRemovedEvent(path));
    }

    private void onModify(Path path) throws IOException {
        eventQueue.add(new FileChangedEvent(path));
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
                final List<WatchEvent<?>> events = key.pollEvents();
                try {
                    for (final WatchEvent<?> event : events) {
                        final Path path = dir.resolve((Path) event.context());
                        if (event.kind() == ENTRY_CREATE) {
                            onAdd(path);
                        } else if (event.kind() == ENTRY_DELETE) {
                            onDelete(path);
                        } else if (event.kind() == ENTRY_MODIFY) {
                            onModify(path);
                        }
                    }
                } finally {
                    key.reset();
                }
            } else {
                if (Files.notExists(dir)) {
                    eventQueue.add(new FileRemovedEvent(dir));
                }
            }
        }
    }

    @Override
    public void close() throws IOException {
        watchService.close();
    }

    @Scheduled(fixedDelay = 100L)
    public void pollQueue() throws IOException {
        for (final Iterator<PropagatedEvent> i = eventQueue.iterator(); i.hasNext(); ) {
            final PropagatedEvent event = i.next();
            if (event instanceof FileChangedEvent) {
                final FileChangedEvent fileChangedEvent = (FileChangedEvent) event;
                eventPublisher.publishEvent(fileChangedEvent);
                i.remove();
            } else if (event instanceof FileAddedEvent) {
                final FileAddedEvent fileAddedEvent = (FileAddedEvent) event;
                final UUID uuid = uuid(fileAddedEvent.getSource());
                if (uuid == null) {
                    fileIds.put(uuid(fileAddedEvent.getSource(), UUID.randomUUID()), fileAddedEvent.getSource());
                }
                eventPublisher.publishEvent(fileAddedEvent);
                i.remove();
            } else {
                final FileRemovedEvent fileRemovedEvent = (FileRemovedEvent) event;
                final Map.Entry<UUID, Path> entry = fileIds.entrySet().parallelStream()
                        .filter(e -> e.getValue().equals(fileRemovedEvent.getSource()))
                        .findAny()
                        .orElse(null);
                if (entry == null) {
                    eventPublisher.publishEvent(fileRemovedEvent);
                    i.remove();
                } else {
                    if (System.currentTimeMillis() - fileRemovedEvent.getTimestamp() > 1000L) {
                        eventPublisher.publishEvent(fileRemovedEvent);
                        i.remove();
                        fileIds.remove(entry.getKey());
                    } else {
                        while (i.hasNext()) {
                            final PropagatedEvent e = i.next();
                            if (e instanceof FileAddedEvent) {
                                final FileAddedEvent fileAddedEvent = (FileAddedEvent) e;
                                final UUID addedUuid = uuid(fileAddedEvent.getSource());
                                if (entry.getKey().equals(addedUuid)) {
                                    entry.setValue(fileAddedEvent.getSource());
                                    i.remove();
                                    eventQueue.remove();
                                    final Path source = fileRemovedEvent.getSource();
                                    final Path target = fileAddedEvent.getSource();
                                    eventPublisher.publishEvent(new FileMovedEvent(source, target));
                                    pollQueue();
                                    return;
                                }
                            }
                        }
                        return;
                    }
                }
            }
        }
    }

    private static UUID uuid(Path path) throws IOException {
        final UserDefinedFileAttributeView xa = Files.getFileAttributeView(path, UserDefinedFileAttributeView.class);
        if (!xa.list().contains("marid-file-id")) {
            return null;
        }
        final ByteBuffer buffer = ByteBuffer.allocate(16);
        final int len = xa.read("marid-file-id", buffer);
        switch (len) {
            case 16:
                return new UUID(buffer.getLong(0), buffer.getLong(8));
            default:
                throw new IllegalStateException("Unable to read marid-file-id from " + xa);
        }
    }

    private static UUID uuid(Path path, UUID uuid) throws IOException {
        final UserDefinedFileAttributeView xa = Files.getFileAttributeView(path, UserDefinedFileAttributeView.class);
        final ByteBuffer buffer = ByteBuffer.allocate(16)
                .putLong(0, uuid.getMostSignificantBits())
                .putLong(8, uuid.getLeastSignificantBits());
        final int len = xa.write("marid-file-id", buffer);
        if (len != 16) {
            throw new IllegalStateException("Unable to write marid-file-id to " + xa);
        }
        return uuid;
    }
}
