package org.marid.ide.event;

import java.nio.file.Path;

/**
 * @author Dmitry Ovchinnikov
 */
public class FileChangedEvent extends PropagatedEvent {

    public FileChangedEvent(Path source) {
        super(source);
    }

    @Override
    public Path getSource() {
        return (Path) super.getSource();
    }
}
