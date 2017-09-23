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

import org.marid.runtime.expression.Expression;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface MaridBean {

    @Nullable
    MaridBean getParent();

    @Nonnull
    String getName();

    @Nonnull
    Expression getFactory();

    @Nonnull
    List<Expression> getInitializers();

    @Nonnull
    List<? extends MaridBean> getChildren();

    @Nonnull
    MaridBean add(@Nonnull String name, @Nonnull Expression factory, @Nonnull Expression... initializers);
}
