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

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.marid.ide.event.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Dmitry Ovchinnikov
 */
@Lazy(false)
@Service
public class TextFilesUpdater {

    private final ApplicationEventPublisher eventPublisher;
    private final PathMatcher textFilesPathMatcher;
    private final ConcurrentMap<Path, StampedHash> hashes = new ConcurrentHashMap<>();

    @Autowired
    public TextFilesUpdater(ApplicationEventPublisher eventPublisher, @Qualifier("text-files") PathMatcher filter) {
        this.eventPublisher = eventPublisher;
        this.textFilesPathMatcher = filter;
    }

    @EventListener
    private void onAdded(FileAddedEvent addedEvent) {
        if (!textFilesPathMatcher.matches(addedEvent.getSource())) {
            return;
        }
        final byte[] newHash = hash(addedEvent.getSource());
        if (newHash == ArrayUtils.EMPTY_BYTE_ARRAY) {
            onRemoved(new FileRemovedEvent(addedEvent.getSource()));
            return;
        }
        final long now = System.currentTimeMillis();
        final Path oldPath = hashes.entrySet().stream()
                .filter(e -> now - e.getValue().timestamp < 500L)
                .filter(e -> Arrays.equals(e.getValue().hash, newHash))
                .findAny()
                .map(Entry::getKey)
                .orElse(null);
        if (oldPath != null) {
            hashes.remove(oldPath);
            eventPublisher.publishEvent(new TextFileRenamedEvent(oldPath, addedEvent.getSource()));
        } else {
            eventPublisher.publishEvent(new TextFileAddedEvent(addedEvent.getSource()));
        }
        hashes.put(addedEvent.getSource(), new StampedHash(newHash, 0L));
    }

    @EventListener
    private void onRemoved(FileRemovedEvent removedEvent) {
        if (!textFilesPathMatcher.matches(removedEvent.getSource())) {
            return;
        }
        hashes.entrySet().stream()
                .filter(e -> e.getKey().equals(removedEvent.getSource()))
                .forEach(e -> e.setValue(new StampedHash(e.getValue().hash, System.currentTimeMillis())));
    }

    @EventListener
    private void onChanged(FileChangedEvent changedEvent) {
        if (!textFilesPathMatcher.matches(changedEvent.getSource())) {
            return;
        }
        final StampedHash oldHash = hashes.get(changedEvent.getSource());
        if (oldHash == null) {
            return;
        }
        final byte[] newHash = hash(changedEvent.getSource());
        if (newHash == ArrayUtils.EMPTY_BYTE_ARRAY) {
            onRemoved(new FileRemovedEvent(changedEvent.getSource()));
            return;
        }
        if (!Arrays.equals(newHash, oldHash.hash)) {
            eventPublisher.publishEvent(new TextFileChangedEvent(changedEvent.getSource()));
        }
    }

    private static byte[] hash(Path path) {
        try (final InputStream inputStream = Files.newInputStream(path)) {
            return DigestUtils.sha1(inputStream);
        } catch (NoSuchFileException x) {
            return ArrayUtils.EMPTY_BYTE_ARRAY;
        } catch (IOException x) {
            throw new UncheckedIOException(x);
        }
    }

    @Scheduled(fixedDelay = 500L)
    public void update() {
        final long now = System.currentTimeMillis();
        hashes.entrySet().removeIf(e -> {
             if (e.getValue().timestamp > 0 && now - e.getValue().timestamp > 500L) {
                 eventPublisher.publishEvent(new TextFileRemovedEvent(e.getKey()));
                 return true;
             } else {
                 return false;
             }
        });
    }

    private static final class StampedHash {

        private final byte[] hash;
        private final long timestamp;

        private StampedHash(byte[] hash, long timestamp) {
            this.hash = hash;
            this.timestamp = timestamp;
        }
    }
}
