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

import org.marid.expression.generic.CallExpression;
import org.marid.expression.generic.ClassExpression;
import org.marid.expression.generic.ThisExpression;
import org.marid.types.TypeContext;
import org.marid.types.TypeUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Stream;

import static org.marid.types.TypeUtils.WILDCARD;

public interface TypedCallExpression extends CallExpression, TypedExpression {

    @Nonnull
    @Override
    TypedExpression getTarget();

    @Nonnull
    @Override
    List<? extends TypedExpression> getArgs();

    @Nonnull
    @Override
    default Type getType(@Nullable Type owner, @Nonnull TypeContext typeContext) {
        final Type targetType = getTarget().resolveType(owner, typeContext);
        if (getTarget() instanceof ClassExpression) { // static call
            if ("new".equals(getMethod())) { // constructor
                return TypeUtils.classType(targetType)
                        .flatMap(tc -> Stream.of(typeContext.getRaw(tc).getConstructors())
                                .filter(e -> TypeUtils.matches(this, e, owner, typeContext))
                                .findFirst()
                                .map(m -> TypeUtils.type(m, getArgs(), owner, typeContext)))
                        .orElse(WILDCARD);
            } else { // static method
                return TypeUtils.classType(targetType)
                        .flatMap(t -> {
                            final Class<?> targetClass = typeContext.getRaw(t);
                            return Stream.of(targetClass.getMethods())
                                    .filter(m -> m.getName().equals(getMethod()) && Modifier.isStatic(m.getModifiers()))
                                    .filter(e -> TypeUtils.matches(this, e, owner, typeContext))
                                    .findFirst()
                                    .map(m -> TypeUtils.type(m, getArgs(), owner, typeContext));
                        })
                        .orElse(WILDCARD);
            }
        } else { // virtual method
            return Stream.of(typeContext.getRaw(targetType).getMethods())
                    .filter(m -> m.getName().equals(getMethod()) && !Modifier.isStatic(m.getModifiers()))
                    .filter(e -> TypeUtils.matches(this, e, owner, typeContext))
                    .findFirst()
                    .map(m -> TypeUtils.type(m, getArgs(), targetType, typeContext))
                    .map(type -> typeContext.resolve(targetType, type))
                    .orElse(WILDCARD);
        }
    }

    @Nonnull
    @Override
    default Type resolve(@Nonnull Type type, @Nonnull TypeContext typeContext) {
        if (getTarget() instanceof ThisExpression && !(type instanceof Class<?>)) {
            return null;
        } else {
            return type;
        }
    }
}
