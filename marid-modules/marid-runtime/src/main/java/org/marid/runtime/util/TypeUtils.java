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

package org.marid.runtime.util;

import org.marid.runtime.expression.Expression;
import org.marid.runtime.types.TypeContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.*;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;

public interface TypeUtils {

    @Nonnull
    static Optional<Type> classType(@Nonnull Type type) {
        if (type instanceof ParameterizedType) {
            final ParameterizedType pt = (ParameterizedType) type;
            final Type[] args = pt.getActualTypeArguments();
            return args.length == 1 && pt.getRawType() == Class.class ? of(args[0]) : empty();
        } else {
            return empty();
        }
    }

    @Nonnull
    static Type type(@Nonnull Type returnType,
                     @Nonnull Type[] argTypes,
                     @Nonnull List<? extends Expression> args,
                     @Nullable Type owner,
                     @Nonnull TypeContext context) {
        return context.evaluate(e -> {
            for (int i = 0; i < argTypes.length; i++) {
                e.where(argTypes[i], args.get(i).getType(owner, context));
            }
            return e.resolve(returnType);
        });
    }

    @Nonnull
    static Type type(@Nonnull Method method,
                     @Nonnull List<? extends Expression> args,
                     @Nullable Type owner,
                     @Nonnull TypeContext context) {
        return type(method.getGenericReturnType(), method.getGenericParameterTypes(), args, owner, context);
    }

    @Nonnull
    static Type type(@Nonnull Constructor<?> constructor,
                     @Nonnull List<? extends Expression> args,
                     @Nullable Type owner,
                     @Nonnull TypeContext context) {
        return type(context.getType(constructor.getDeclaringClass()), constructor.getGenericParameterTypes(), args, owner, context);
    }

    @Nonnull
    static Optional<Class<?>> getClass(@Nonnull ClassLoader classLoader, @Nonnull String name) {
        try {
            return Optional.of(classLoader.loadClass(name));
        } catch (ClassNotFoundException x) {
            return Optional.empty();
        }
    }

    @Nonnull
    static Optional<Field> getField(@Nonnull Class<?> type, String name) {
        try {
            return Optional.of(type.getField(name));
        } catch (NoSuchFieldException x) {
            return Optional.empty();
        }
    }
}
