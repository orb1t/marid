package org.marid.ide.event;

import java.nio.file.Path;

/**
 * @author Dmitry Ovchinnikov
 */
public class TextFileChangedEvent extends PropagatedEvent {

    public TextFileChangedEvent(Path source) {
        super(source);
    }

    @Override
    public Path getSource() {
        return (Path) super.getSource();
    }
}
