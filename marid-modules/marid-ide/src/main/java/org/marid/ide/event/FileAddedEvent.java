package org.marid.ide.event;

import java.nio.file.Path;

/**
 * @author Dmitry Ovchinnikov
 */
public class FileAddedEvent extends PropagatedEvent {

    public FileAddedEvent(Path source) {
        super(source);
    }

    @Override
    public Path getSource() {
        return (Path) super.getSource();
    }
}
