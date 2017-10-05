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

package org.marid.runtime.expression;

import org.marid.runtime.types.TypeContext;
import org.marid.runtime.types.TypeUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;

import static java.util.Collections.emptyMap;

public interface FieldGetExpression extends Expression {

    @Nonnull
    Expression getTarget();

    @Nonnull
    String getField();

    @Nonnull
    @Override
    default Type getType(@Nullable Type owner, @Nonnull TypeContext typeContext) {
        final Type targetType = getTarget().getType(owner, typeContext);
        final Class<?> targetClass = typeContext.getRaw(targetType);
        return TypeUtils.getField(targetClass, typeContext.resolvePlaceholders(getField()))
                .map(f -> typeContext.resolve(targetType, f.getGenericType(), emptyMap()))
                .map(t -> owner != null ? typeContext.resolve(owner, t, emptyMap()) : t)
                .orElseGet(typeContext::getWildcard);
    }
}
