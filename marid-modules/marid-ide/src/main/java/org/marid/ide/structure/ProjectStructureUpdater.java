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

import org.marid.ide.event.FileAddedEvent;
import org.marid.ide.event.FileChangedEvent;
import org.marid.ide.event.FileMovedEvent;
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
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.StandardWatchEventKinds.*;
import static java.util.logging.Level.*;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
@Lazy(false)
@Service
public class ProjectStructureUpdater implements Closeable {

    private static final Kind<?>[] EVENTS = {ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY};
    private static final UUID EMPTY_FILE_ID = new UUID(0L, 0L);

    private final Path root;
    private final ApplicationEventPublisher eventPublisher;
    private final ScheduledExecutorService scheduledExecutorService;
    private final WatchService watchService;
    private final ConcurrentSkipListMap<Path, WatchKey> watchKeyMap = new ConcurrentSkipListMap<>();
    private final Map<UUID, Path> fileIds = new ConcurrentHashMap<>();

    @Autowired
    public ProjectStructureUpdater(ProjectManager projectManager,
                                   ApplicationEventPublisher eventPublisher,
                                   ScheduledExecutorService scheduledExecutorService) throws IOException {
        this.root = projectManager.getProfilesDir();
        this.eventPublisher = eventPublisher;
        this.scheduledExecutorService = scheduledExecutorService;
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
            final UUID fileId = uuid(path);
            final Path oldFile = fileIds.get(fileId);
            if (oldFile != null) {
                eventPublisher.publishEvent(new FileMovedEvent(oldFile, path));
                fileIds.put(fileId, path);
            } else {
                fileIds.put(uuid(path, UUID.randomUUID()), path);
                eventPublisher.publishEvent(new FileAddedEvent(path));
            }
            if (Files.isDirectory(path)) {
                if (!watchKeyMap.containsKey(path)) {
                    watchKeyMap.put(path, path.register(watchService, EVENTS));
                }
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
        final Map<Path, WatchKey> subMap = watchKeyMap.subMap(path, path.resolve("\uFFFF"));
        subMap.values().forEach(WatchKey::cancel);
        subMap.clear();
        scheduledExecutorService.schedule(() -> {
            if (fileIds.values().removeIf(path::equals)) {
                eventPublisher.publishEvent(new FileRemovedEvent(path));
            }
        }, 1000L, TimeUnit.MILLISECONDS);
    }

    private void onModify(Path path) throws IOException {
        eventPublisher.publishEvent(new FileChangedEvent(path));
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
                log(CONFIG, "Unregister {0}", dir);
                watchKeyMap.remove(dir);
            }
        }
    }

    @Override
    public void close() throws IOException {
        watchService.close();
    }

    private static UUID uuid(Path path) throws IOException {
        final UserDefinedFileAttributeView xa = Files.getFileAttributeView(path, UserDefinedFileAttributeView.class);
        if (!xa.list().contains("marid-file-id")) {
            return EMPTY_FILE_ID;
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
