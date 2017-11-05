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

import org.marid.expression.generic.SetExpression;
import org.marid.expression.generic.ThisExpression;
import org.marid.types.TypeContext;
import org.marid.types.TypeUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;

public interface TypedSetExpression extends SetExpression, TypedExpression {

    @Nonnull
    @Override
    TypedExpression getTarget();

    @Nonnull
    @Override
    TypedExpression getValue();

    @Nonnull
    @Override
    default Type getType(@Nullable Type owner, @Nonnull TypeContext typeContext) {
        return getTarget().getType(owner, typeContext);
    }

    @Nonnull
    @Override
    default Type resolve(@Nonnull Type type, @Nonnull TypeContext typeContext) {
        if (getTarget() instanceof ThisExpression && !(type instanceof Class<?>)) {
            return TypeUtils.getField(typeContext.getRaw(type), getField())
                    .map(f -> typeContext.resolve(type, f.getGenericType()))
                    .map(t -> typeContext.evaluate(e -> e.where(t, getValue()
                            .resolveType(type, typeContext))
                            .resolve(type)))
                    .orElse(type);
        } else {
            return type;
        }
    }
}
