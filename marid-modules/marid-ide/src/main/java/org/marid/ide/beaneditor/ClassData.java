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

package org.marid.ide.beaneditor;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.beans.Introspector.decapitalize;
import static java.util.stream.Collectors.toMap;

/**
 * @author Dmitry Ovchinnikov
 */
public class ClassData {

    private final Class<?> type;
    private final Map<String, Parameter> parameters;
    private final Map<String, Method> getters;
    private final Map<String, Method> setters;
    private final Map<String, Method> procedures;
    private final Map<String, Method> factoryMethods;

    public ClassData(Class<?> type) {
        this.type = type;
        this.parameters = Stream.of(type.getConstructors())
                .findFirst()
                .map(constructor -> {
                    final Map<String, Parameter> map = new LinkedHashMap<>();
                    for (final Parameter parameter : constructor.getParameters()) {
                        map.put(parameter.getName(), parameter);
                    }
                    return map;
                })
                .orElse(Collections.emptyMap());
        this.getters = Stream.of(type.getMethods())
                .filter(m -> m.getParameterCount() == 0)
                .filter(m -> isGetter(m.getName()))
                .filter(m -> isAllowed(m.getReturnType()))
                .collect(toMap(m -> {
                    if (m.getName().startsWith("get")) {
                        return decapitalize(m.getName().substring(3));
                    } else {
                        return decapitalize(m.getName().substring(2));
                    }
                }, m -> m));
        this.setters = Stream.of(type.getMethods())
                .filter(m -> m.getParameterCount() == 1)
                .filter(m -> m.getReturnType().getName().equals("void"))
                .filter(m -> isAllowed(m.getParameterTypes()[0]))
                .filter(m -> isSetter(m.getName()))
                .collect(toMap(e -> decapitalize(e.getName().substring(3)), e -> e, (m1, m2) -> m2, TreeMap::new));
        this.factoryMethods = Stream.of(type.getMethods())
                .filter(m -> m.getParameterCount() == 0)
                .filter(m -> m.getReturnType() != void.class)
                .collect(toMap(Method::getName, e -> e, (m1, m2) -> m2, TreeMap::new));
        this.procedures = new TreeMap<>();
        final Predicate<Method> closePredicate = AutoCloseable.class.isAssignableFrom(type)
                ? m -> !"close".equals(m.getName())
                : m -> true;
        for (Class<?> c = type; c.getSuperclass() != null; c = c.getSuperclass()) {
            for (final Method m : c.getDeclaredMethods()) {
                if (m.getReturnType() == void.class
                        && m.getParameterCount() == 0
                        && !m.isAnnotationPresent(PreDestroy.class)
                        && !m.isAnnotationPresent(PostConstruct.class)
                        && closePredicate.test(m)) {
                    procedures.put(m.getName(), m);
                }
            }
        }
    }

    private static boolean isGetter(String name) {
        return name.startsWith("get") && name.length() > 3 || name.startsWith("is") && name.length() > 2;
    }

    private static boolean isSetter(String name) {
        return name.startsWith("set");
    }

    private static boolean isAllowed(Class<?> type) {
        return !type.isPrimitive() && Modifier.isPublic(type.getModifiers());
    }

    public int getParameterIndex(String name) {
        final Iterator<String> it = parameters.keySet().iterator();
        for (int i = 0; it.hasNext(); i++) {
            final String key = it.next();
            if (key.equals(name)) {
                return i;
            }
        }
        return -1;
    }

    public Parameter getParameter(int index) {
        final Iterator<Parameter> it = parameters.values().iterator();
        for (int i = 0; it.hasNext(); i++) {
            final Parameter parameter = it.next();
            if (index == i) {
                return parameter;
            }
        }
        return null;
    }

    public Map<String, Parameter> getParameters() {
        return parameters;
    }

    public Map<String, Method> getGetters() {
        return getters;
    }

    public Map<String, Method> getFactoryMethods() {
        return factoryMethods;
    }

    public Map<String, Method> getSetters() {
        return setters;
    }

    public Map<String, Method> getProcedures() {
        return procedures;
    }

    public boolean isAssignableFrom(ClassData classData) {
        return type.isAssignableFrom(classData.type);
    }

    public Class<?> getType() {
        return type;
    }
}
