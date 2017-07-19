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

package org.marid.runtime.beans;

import javax.annotation.Nonnull;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.stream.Stream;

import static java.lang.String.format;

/**
 * @author Dmitry Ovchinnikov
 */
public final class MaridFactoryBean {

    @Nonnull
    public final String name;

    @Nonnull
    public final String[] argTypes;

    @Nonnull
    public final Lookup lookup = MethodHandles.publicLookup();

    public MaridFactoryBean(@Nonnull String producer) {
        final int index = producer.indexOf('(');
        if (index < 0) {
            name = producer;
            argTypes = new String[0];
        } else {
            name = producer.substring(0, index);
            argTypes = Stream.of(producer.substring(index + 1, producer.length() - 1).split(","))
                    .map(String::trim)
                    .filter(e -> !e.isEmpty())
                    .toArray(String[]::new);
        }
    }

    private boolean matches(Executable executable) {
        if (argTypes.length == executable.getParameterCount()) {
            final Class<?>[] argTypes = executable.getParameterTypes();
            for (int i = 0; i < argTypes.length; i++) {
                if (!this.argTypes[i].equals(argTypes[i].getName())) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public MethodHandle[] findProperties(Bean bean, MethodHandle constructor) throws Exception {
        final Class<?> targetClass = constructor.type().returnType();
        final MethodHandle[] handles = new MethodHandle[bean.props.length];
        for (int i = 0; i < handles.length; i++) {
            final String propName;
            final String filter;
            final int filterIndex = bean.props[i].name.lastIndexOf("#");
            if (filterIndex < 0) {
                filter = null;
                propName = bean.props[i].name;
            } else {
                filter = bean.props[i].name.substring(filterIndex + 1);
                propName = bean.props[i].name.substring(filterIndex);
            }
            for (final Method method : targetClass.getMethods()) {
                if (method.getParameterCount() == 1 && method.getName().equals(propName)) {
                    handles[i] = filtered(filter, lookup.unreflect(method));
                    break;
                }
            }
            if (handles[i] == null) {
                for (final Field field : targetClass.getFields()) {
                    if (field.getName().equals(propName)) {
                        handles[i] = filtered(filter, lookup.unreflectSetter(field));
                        break;
                    }
                }
            }
            if (handles[i] == null) {
                throw new IllegalStateException(format("No property found: %s", bean.props[i]));
            }
        }
        return handles;
    }

    public MethodHandle filtered(String filter, MethodHandle handle) throws Exception {
        if (filter == null) {
            return handle;
        } else {
            final Class<?> type = handle.type().returnType();
            try {
                final Method method = type.getMethod(filter);
                if (Modifier.isStatic(method.getModifiers())) {
                    throw new NoSuchMethodException(filter);
                }
                return MethodHandles.filterReturnValue(handle, lookup.unreflect(method));
            } catch (NoSuchMethodException e1) {
                try {
                    final Field field = type.getField(filter);
                    if (Modifier.isStatic(field.getModifiers())) {
                        throw new NoSuchFieldException(filter);
                    }
                    return MethodHandles.filterReturnValue(handle, lookup.unreflectGetter(field));
                } catch (NoSuchFieldException x) {
                    throw new IllegalArgumentException("No filters found: " + filter);
                }
            }
        }
    }

    public MethodHandle findProducer(Class<?> type) throws IllegalAccessException {
        switch (name) {
            case "new": {
                for (final Constructor<?> c : type.getConstructors()) {
                    if (matches(c)) {
                        return lookup.unreflectConstructor(c);
                    }
                }
                break;
            }
            default: {
                for (final Method m : type.getMethods()) {
                    if (m.getName().equals(name) && matches(m)) {
                        return lookup.unreflect(m);
                    }
                }
                if (argTypes.length == 0) {
                    for (final Field f : type.getFields()) {
                        if (f.getName().equals(name)) {
                            return lookup.unreflectGetter(f);
                        }
                    }
                }
                break;
            }
        }
        throw new IllegalStateException(format("Producer not found: %s(%s)", name, Arrays.toString(argTypes)));
    }
}
