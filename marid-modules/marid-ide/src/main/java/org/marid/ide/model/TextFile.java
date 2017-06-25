/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
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
