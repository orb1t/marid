/*
 * Copyright 2017 Dmitry Ovchinnikov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.marid.ide.structure;

import org.marid.ide.event.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

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
    public void onAdded(FileAddedEvent addedEvent) {
        eventPublisher.publishEvent(new TextFileAddedEvent(addedEvent.getSource()));
    }

    @EventListener(condition = "@textFilePathMatcher.matches(#root.event.source)")
    public void onRemoved(FileRemovedEvent removedEvent) {
        eventPublisher.publishEvent(new TextFileRemovedEvent(removedEvent.getSource()));
    }

    @EventListener(condition = "@textFilePathMatcher.matches(#root.event.source)")
    public void onChanged(FileChangedEvent changedEvent) {
        eventPublisher.publishEvent(new TextFileChangedEvent(changedEvent.getSource()));
    }

    @EventListener(condition = "@textFilePathMatcher.matches(#root.event.source)")
    public void onMoved(FileMovedEvent movedEvent) {
        eventPublisher.publishEvent(new TextFileMovedEvent(movedEvent.getSource(), movedEvent.getTarget()));
    }
}
