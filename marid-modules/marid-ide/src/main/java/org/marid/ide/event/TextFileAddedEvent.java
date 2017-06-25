package org.marid.ide.event;

import java.nio.file.Path;

/**
 * @author Dmitry Ovchinnikov
 */
public class TextFileAddedEvent extends PropagatedEvent {

    public TextFileAddedEvent(Path source) {
        super(source);
    }

    @Override
    public Path getSource() {
        return (Path) super.getSource();
    }
}
