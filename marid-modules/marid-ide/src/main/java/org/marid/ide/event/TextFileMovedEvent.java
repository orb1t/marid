package org.marid.ide.event;

import java.nio.file.Path;

/**
 * @author Dmitry Ovchinnikov
 */
public class TextFileMovedEvent extends PropagatedEvent {

    private final Path target;

    public TextFileMovedEvent(Path source, Path target) {
        super(source);
        this.target = target;
    }

    @Override
    public Path getSource() {
        return (Path) super.getSource();
    }

    public Path getTarget() {
        return target;
    }
}
