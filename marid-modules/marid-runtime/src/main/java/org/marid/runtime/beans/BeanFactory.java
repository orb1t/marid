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

import static java.util.Objects.requireNonNull;

/**
 * @author Dmitry Ovchinnikov
 */
public final class BeanFactory {

    @Nonnull
    public final String type;

    @Nonnull
    public final String value;

    public BeanFactory(@Nonnull String type, @Nonnull String value) {
        this.type = type;
        this.value = value;
    }

    public BeanFactory(@Nonnull Element element) {
        this.type = requireNonNull(element.getAttribute("ft"));
        this.value = requireNonNull(element.getAttribute("fv"));
    }

    public void writeTo(@Nonnull Element element) {
        element.setAttribute("ft", type);
        element.setAttribute("fv", value);
    }

    @Override
    public String toString() {
        return String.format("Factory(%s,%s)", type, value);
    }
}
