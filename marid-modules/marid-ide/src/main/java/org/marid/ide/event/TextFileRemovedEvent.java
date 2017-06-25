package org.marid.ide.event;

import java.nio.file.Path;

/**
 * @author Dmitry Ovchinnikov
 */
public class TextFileRemovedEvent extends PropagatedEvent {

    public TextFileRemovedEvent(Path source) {
        super(source);
    }

    @Override
    public Path getSource() {
        return (Path) super.getSource();
    }
}
