/*-
 * #%L
 * marid-runtime
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.marid.runtime.model;

import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.marid.io.Xmls.content;

public class MaridRuntimeArgument implements MaridArgument {

    private final MaridRuntimeMethod parent;
    private final String value;

    public MaridRuntimeArgument(@Nonnull MaridRuntimeMethod parent, @Nullable String value) {
        this.parent = parent;
        this.value = value;
    }

    public MaridRuntimeArgument(@Nonnull MaridRuntimeMethod parent, @Nonnull Element element) {
        this.parent = parent;
        this.value = content(element).orElse(null);
    }

    @Nonnull
    @Override
    public MaridMethod getParent() {
        return parent;
    }

    @Nullable
    @Override
    public String getValue() {
        return value;
    }

    public void writeTo(Element element) {
        if (element != null) element.setTextContent(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
