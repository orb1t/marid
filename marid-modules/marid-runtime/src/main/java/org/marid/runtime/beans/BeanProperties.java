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
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Type;

/**
 * @author Dmitry Ovchinnikov
 */
public final class BeanProperties {

    @Nonnull
    public final Type[] types;

    @Nonnull
    public final MethodHandle[] setters;

    public BeanProperties(@Nonnull Type[] types, @Nonnull MethodHandle[] setters) {
        this.types = types;
        this.setters = setters;
    }
}
