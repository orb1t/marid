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

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * @author Dmitry Ovchinnikov
 */
public final class BeanMethodArg {

    @Nonnull
    public final String name;

    @Nonnull
    public final String type;

    @Nullable
    public final String filter;

    @Nullable
    public final String value;

    public BeanMethodArg(@Nonnull String name, @Nonnull String type, @Nullable String filter, @Nullable String value) {
        this.name = name;
        this.type = type;
        this.filter = filter;
        this.value = value;
    }

    public BeanMethodArg(@Nonnull Element element) {
        type = requireNonNull(element.getAttribute("type"));
        name = requireNonNull(element.getAttribute("name"));
        filter = element.getAttribute("filter");
        value = element.getTextContent();
    }

    public void writeTo(@Nonnull Element element) {
        element.setAttribute("type", type);
        element.setAttribute("name", name);
        element.setAttribute("filter", filter);
        element.setTextContent(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            final BeanMethodArg that = (BeanMethodArg) o;
            return Objects.equals(name, that.name) &&
                    Objects.equals(type, that.type) &&
                    Objects.equals(filter, that.filter) &&
                    Objects.equals(value, that.value);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, filter, value);
    }

    @Override
    public String toString() {
        return String.format("%s(%s,%s,%s)", name, type, filter, value);
    }
}
