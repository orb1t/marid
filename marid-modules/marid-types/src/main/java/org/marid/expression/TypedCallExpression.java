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
import org.marid.runtime.util.TypeUtils;
import org.marid.types.TypeContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Executable;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

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
        final String methodName = typeContext.resolvePlaceholders(getMethod());
        final Type targetType = getTarget().getType(owner, typeContext);
        final Predicate<Executable> executableMatcher = e -> {
            if (e.getParameterCount() == getArgs().size()) {
                final Type[] pt = e.getGenericParameterTypes();
                for (int i = 0; i < pt.length; i++) {
                    final Type at = getArgs().get(i).getType(owner, typeContext);
                    if (!typeContext.isAssignable(pt[i], at)) {
                        return false;
                    }
                }
                return true;
            } else {
                return false;
            }
        };
        if (getTarget() instanceof ClassExpression) { // static call
            if ("new".equals(methodName)) { // constructor
                return TypeUtils.classType(targetType)
                        .flatMap(tc -> Stream.of(typeContext.getRaw(tc).getConstructors())
                                .filter(executableMatcher)
                                .findFirst()
                                .map(m -> TypeUtils.type(m, getArgs(), owner, typeContext)))
                        .orElseGet(typeContext::getWildcard);
            } else { // static method
                return TypeUtils.classType(targetType)
                        .flatMap(t -> {
                            final Class<?> targetClass = typeContext.getRaw(t);
                            return Stream.of(targetClass.getMethods())
                                    .filter(m -> m.getName().equals(methodName))
                                    .filter(m -> Modifier.isStatic(m.getModifiers()))
                                    .filter(executableMatcher)
                                    .findFirst()
                                    .map(m -> TypeUtils.type(m, getArgs(), owner, typeContext));
                        })
                        .orElseGet(typeContext::getWildcard);
            }
        } else { // virtual method
            return Stream.of(typeContext.getRaw(targetType).getMethods())
                    .filter(m -> m.getName().equals(methodName))
                    .filter(m -> !Modifier.isStatic(m.getModifiers()))
                    .filter(executableMatcher)
                    .findFirst()
                    .map(m -> TypeUtils.type(m, getArgs(), targetType, typeContext))
                    .map(type -> typeContext.resolve(targetType, type))
                    .orElseGet(typeContext::getWildcard);
        }
    }
}
