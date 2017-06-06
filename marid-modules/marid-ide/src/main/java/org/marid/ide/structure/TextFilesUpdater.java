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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import static java.util.logging.Level.CONFIG;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
@Lazy(false)
@Service
public class TextFilesUpdater {

    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public TextFilesUpdater(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @EventListener(condition = "@textFilePathMatcher.matches(#root.event.source)")
    private void onAdded(FileAddedEvent addedEvent) {
        log(CONFIG, "{0}", addedEvent);
        eventPublisher.publishEvent(new TextFileChangedEvent(addedEvent.getSource()));
    }

    @EventListener(condition = "@textFilePathMatcher.matches(#root.event.source)")
    private void onRemoved(FileRemovedEvent removedEvent) {
        log(CONFIG, "{0}", removedEvent);
        eventPublisher.publishEvent(new TextFileRemovedEvent(removedEvent.getSource()));
    }

    @EventListener(condition = "@textFilePathMatcher.matches(#root.event.source)")
    private void onChanged(FileChangedEvent changedEvent) {
        log(CONFIG, "{0}", changedEvent);
        eventPublisher.publishEvent(new TextFileChangedEvent(changedEvent.getSource()));
    }

    @EventListener(condition = "@textFilePathMatcher.matches(#root.event.source)")
    private void onMoved(FileMovedEvent movedEvent) {
        log(CONFIG, "{0}", movedEvent);
        eventPublisher.publishEvent(new TextFileMovedEvent(movedEvent.getSource(), movedEvent.getTarget()));
    }
}
