/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
 * Marid, the free data acquisition and visualization software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.marid.util;

import org.springframework.core.ResolvableType;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.of;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
public interface MethodUtils {

    static String methodText(Method method) {
        return of(method.getParameters())
                .map(Parameter::getParameterizedType)
                .map(t -> {
                    if (t instanceof Class<?>) {
                        final Class<?> klass = (Class<?>) t;
                        if (klass.getName().startsWith("java.lang.")) {
                            return klass.getSimpleName();
                        }
                        return klass.getName();
                    } else {
                        return t.toString();
                    }
                })
                .collect(joining(",", method.getName() + "(", ") : " + method.getGenericReturnType()));
    }

    static String readableType(ResolvableType type) {
        return Stream.of("java.util.function.", "java.util.", "java.lang.", "java.io.", "java.nio.")
                .reduce(type.toString(), (a, e) -> a.replace(e, ""));
    }
}
