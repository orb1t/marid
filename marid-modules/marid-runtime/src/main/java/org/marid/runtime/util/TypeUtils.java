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

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.IntFunction;

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
    static Map<Type, Type> map(@Nonnull Type[] formalTypes, @Nonnull IntFunction<Type> actualTypeFunc) {
        final LinkedHashMap<Type, Type> map = new LinkedHashMap<>(formalTypes.length);
        for (int i = 0; i < formalTypes.length; i++) {
            map.put(formalTypes[i], actualTypeFunc.apply(i));
        }
        return map;
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
