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

package org.marid.runtime.beans;

import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

/**
 * @author Dmitry Ovchinnikov
 */
public final class BeanMember {

    @Nonnull
    public final String type;

    @Nonnull
    public final String name;

    @Nullable
    public final String value;

    public BeanMember(@Nonnull String type, @Nonnull String name, @Nullable String value) {
        this.type = type;
        this.name = name;
        this.value = value;
    }

    public BeanMember(@Nonnull Element element) {
        type = requireNonNull(element.getAttribute("type"));
        name = requireNonNull(element.getAttribute("name"));
        value = element.getTextContent();
    }

    public void writeTo(@Nonnull Element element) {
        element.setAttribute("type", type);
        element.setAttribute("name", name);
        element.setTextContent(value);
    }

    @Override
    public String toString() {
        return String.format("Member(%s,%s,%s)", type, name, value);
    }
}
