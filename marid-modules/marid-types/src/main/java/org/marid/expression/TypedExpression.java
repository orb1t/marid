/*-
 * #%L
 * marid-types
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

package org.marid.expression;

import org.marid.expression.generic.Expression;
import org.marid.types.TypeContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.List;

public interface TypedExpression extends Expression {

    @Nonnull
    @Override
    List<? extends TypedExpression> getInitializers();

    @Nonnull
    Type getType(@Nullable Type owner, @Nonnull TypeContext typeContext);

    @Nonnull
    default Type resolve(@Nonnull Type type, @Nonnull TypeContext typeContext) {
        return type;
    }

    @Nonnull
    default Type resolveType(@Nullable Type owner, @Nonnull TypeContext typeContext) {
        final Type type = getType(owner, typeContext);
        if (type instanceof Class<?>) {
            return type;
        } else {
            return getInitializers().stream().reduce(type, (t, i) -> i.resolve(t, typeContext), (t1, t2) -> t2);
        }
    }
}
