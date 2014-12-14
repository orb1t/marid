/*
 * Copyright (C) 2014 Dmitry Ovchinnikov
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

package org.marid.service;

import org.marid.functions.SafeBiFunction;
import org.marid.reflect.IntrospectionUtils;
import org.marid.util.Utils;
import org.springframework.core.env.Environment;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.function.Function;

import static java.util.Collections.emptyList;
import static org.marid.util.MaridClassValue.getCallContext;
import static org.marid.util.Utils.currentClassLoader;

/**
 * @author Dmitry Ovchinnikov.
 */
public interface MaridServiceConfig extends ServiceParameters {

    static <T extends MaridServiceConfig> T config() {
        return new CallerContext(getCallContext()).newProxy(c -> c.invocationHandler((proxy, method) -> {
            for (final Annotation a : c.annotationMap.getOrDefault(method.getDeclaringClass(), emptyList())) {
                final Object value = method.invoke(a);
                if (!Objects.deepEquals(method.getDefaultValue(), value)) {
                    return value;
                }
            }
            return method.getDefaultValue();
        }));
    }

    static <T extends MaridServiceConfig> T config(String prefix, Environment environment) {
        return new CallerContext(getCallContext()).newProxy(c -> c.invocationHandler((proxy, method) -> {
            final Object v = environment.getProperty(prefix + "." + method.getName(), method.getReturnType());
            if (v == null) {
                for (final Annotation a : c.annotationMap.getOrDefault(method.getDeclaringClass(), emptyList())) {
                    final Object value = method.invoke(a);
                    if (!Objects.deepEquals(method.getDefaultValue(), value)) {
                        return value;
                    }
                }
                return method.getDefaultValue();
            } else {
                return v;
            }
        }));
    }

    class CallerContext {

        final Class<?> type;
        final List<Class<? extends Annotation>> declaredAnnotations;
        final Map<Class<?>, List<Annotation>> annotationMap = new IdentityHashMap<>();

        CallerContext(Class<?>[] stack) {
            final Set<Class<?>> classes = new HashSet<>();
            for (final Class<?> c : stack) {
                for (Class<?> k = c; AbstractMaridService.class.isAssignableFrom(k); k = k.getSuperclass()) {
                    classes.add(k);
                }
            }
            type = classes.stream().reduce(MaridServiceConfig.class, (a, c) -> {
                for (final Constructor<?> cs : c.getDeclaredConstructors()) {
                    if (cs.getParameterCount() == 1 && a.isAssignableFrom(cs.getParameterTypes()[0])) {
                        return cs.getParameterTypes()[0];
                    }
                }
                return a;
            });
            declaredAnnotations = IntrospectionUtils.getAnnotationClasses(type);
            for (int i = stack.length - 1; i >= 0; i--) {
                final Class<?> c = stack[i];
                if (AbstractMaridService.class.isAssignableFrom(c)) {
                    for (final Class<? extends Annotation> ac : declaredAnnotations) {
                        final Annotation a = c.getAnnotation(ac);
                        if (a != null) {
                            annotationMap.computeIfAbsent(c, v -> new ArrayList<>()).add(a);
                        }
                    }
                }
            }
        }

        <T> T newProxy(Function<CallerContext, InvocationHandler> function) {
            return Utils.cast(Proxy.newProxyInstance(currentClassLoader(), new Class<?>[]{type}, function.apply(this)));
        }

        InvocationHandler invocationHandler(SafeBiFunction<Object, Method, Object> function) {
            return (proxy, method, args) -> {
                switch (method.getName()) {
                    case "getAnnotationType":
                        return proxy.getClass();
                    case "hashCode":
                        return System.identityHashCode(proxy);
                    case "equals":
                        return proxy == args[0];
                    case "toString":
                        return Integer.toHexString(proxy.hashCode());
                    default:
                        return method.getParameterCount() == 0 ? function.applyUnsafe(proxy, method) : null;
                }
            };
        }
    }
}
