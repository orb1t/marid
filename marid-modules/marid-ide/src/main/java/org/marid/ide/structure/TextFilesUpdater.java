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
