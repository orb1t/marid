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
