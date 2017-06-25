package org.marid.ide.model;

import org.marid.jfx.beans.AbstractObservable;
import org.marid.jfx.beans.OProp;

import java.nio.file.Path;

/**
 * @author Dmitry Ovchinnikov
 */
public class TextFile extends AbstractObservable {

    public final OProp<Path> path = new OProp<>("path");

    public TextFile(Path path) {
        this.path.set(path);
        this.path.addListener(o -> fireInvalidate(this));
    }

    public OProp<Path> pathProperty() {
        return path;
    }

    public Path getPath() {
        return path.get();
    }

    public void setPath(Path path) {
        this.path.set(path);
    }

    @Override
    public int hashCode() {
        return path.get().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || obj.getClass() != getClass()) {
            return false;
        } else {
            final TextFile that = (TextFile) obj;
            return that.path.get().equals(path.get());
        }
    }
}
