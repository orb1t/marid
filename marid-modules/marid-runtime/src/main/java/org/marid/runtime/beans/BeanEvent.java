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

import javax.annotation.Nonnull;
import java.util.EventObject;

/**
 * @author Dmitry Ovchinnikov
 */
public final class BeanEvent extends EventObject {

    @Nonnull
    private final String name;

    @Nonnull
    private final String type;

    public BeanEvent(@Nonnull Object source, @Nonnull String name, @Nonnull String type) {
        super(source);
        this.name = name;
        this.type = type;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return String.format("%s[%s,%s]", getClass().getSimpleName(), type, name);
    }
}
