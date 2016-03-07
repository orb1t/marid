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

package org.marid.ide.beaned.data;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanInfo {

    private static final Set<Method> OBJECT_METHODS = Stream.of(Object.class.getMethods()).collect(toSet());

    private final Class<?> type;
    private final List<Method> methods;
    private final List<Constructor<?>> constructors;
    private final List<PropertyDescriptor> propertyDescriptors;

    public BeanInfo(Class<?> type) throws Exception {
        this.type = type;
        methods = Stream.of(type.getMethods())
                .filter(m -> m.getParameterCount() == 0)
                .filter(m -> !OBJECT_METHODS.contains(m))
                .collect(Collectors.toList());
        constructors = Stream.of(type.getConstructors())
                .filter(c -> c.getParameterCount() > 0)
                .collect(Collectors.toList());
        Introspector.flushCaches();
        propertyDescriptors = Arrays.asList(Introspector.getBeanInfo(type).getPropertyDescriptors());
    }

    public BeanInfo() {
        type = Void.class;
        methods = Collections.emptyList();
        constructors = Collections.emptyList();
        propertyDescriptors = Collections.emptyList();
    }

    public Class<?> getType() {
        return type;
    }

    public List<Method> getMethods() {
        return methods;
    }

    public List<Constructor<?>> getConstructors() {
        return constructors;
    }

    public List<PropertyDescriptor> getPropertyDescriptors() {
        return propertyDescriptors;
    }
}
