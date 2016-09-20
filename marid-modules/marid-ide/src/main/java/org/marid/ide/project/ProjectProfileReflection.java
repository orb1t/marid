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

package org.marid.ide.project;

import org.marid.spring.xml.data.BeanArg;
import org.marid.spring.xml.data.BeanData;
import org.marid.spring.xml.data.BeanProp;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.marid.misc.Reflections.parameterName;

/**
 * @author Dmitry Ovchinnikov
 */
public class ProjectProfileReflection {

    private final ProjectProfile profile;

    public ProjectProfileReflection(ProjectProfile profile) {
        this.profile = profile;
    }

    public Stream<? extends Executable> getConstructors(BeanData data) {
        if (data.isFactoryBean()) {
            if (data.factoryBean.isNotEmpty().get()) {
                return profile.getBeanFiles().stream()
                        .flatMap(e -> e.getValue().allBeans())
                        .filter(b -> data.factoryBean.isEqualTo(b.nameProperty()).get())
                        .map(this::getClass)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .flatMap(t -> Stream.of(t.getMethods()))
                        .filter(m -> m.getReturnType() != void.class)
                        .filter(m -> data.factoryMethod.isEqualTo(m.getName()).get())
                        .sorted(comparingInt(Method::getParameterCount));
            } else {
                return profile.getClass(data.type.get())
                        .map(type -> Stream.of(type.getMethods())
                                .filter(m -> Modifier.isStatic(m.getModifiers()))
                                .filter(m -> m.getReturnType() != void.class)
                                .filter(m -> data.factoryMethod.isEqualTo(m.getName()).get())
                                .sorted(comparingInt(Method::getParameterCount)))
                        .orElse(Stream.empty());
            }
        } else {
            return getClass(data)
                    .map(c -> Stream.of(c.getConstructors()).sorted(comparingInt(Constructor::getParameterCount)))
                    .orElseGet(Stream::empty);
        }
    }

    public Optional<? extends Executable> getConstructor(BeanData data) {
        final List<? extends Executable> executables = getConstructors(data).collect(toList());
        switch (executables.size()) {
            case 0:
                return Optional.empty();
            case 1:
                return Optional.of(executables.get(0));
            default:
                final Class<?>[] types = data.beanArgs.stream()
                        .map(a -> profile.getClass(a.type.get()).orElse(Object.class))
                        .toArray(Class<?>[]::new);
                return executables.stream().filter(m -> Arrays.equals(types, m.getParameterTypes())).findFirst();
        }
    }

    public Optional<Class<?>> getClass(BeanData data) {
        if (data.isFactoryBean()) {
            return getConstructor(data).map(e -> ((Method) e).getReturnType());
        } else {
            return profile.getClass(data.type.get());
        }
    }

    public Optional<? extends Type> getType(BeanData data) {
        if (data.isFactoryBean()) {
            return getConstructor(data).map(e -> ((Method) e).getGenericReturnType());
        } else {
            return profile.getClass(data.type.get());
        }
    }

    public Optional<? extends Type> getArgType(BeanData data, String name) {
        final Optional<? extends Executable> c = getConstructor(data);
        if (c.isPresent()) {
            final Parameter[] parameters = c.get().getParameters();
            final Optional<Parameter> parameter = Stream.of(parameters).filter(p -> p.getName().equals(name)).findAny();
            if (parameter.isPresent()) {
                return Optional.of(parameter.get().getParameterizedType());
            }
        }
        return Optional.empty();
    }

    public Optional<? extends Type> getPropType(BeanData data, String name) {
        final Optional<PropertyDescriptor> pd = getPropertyDescriptors(data)
                .filter(d -> d.getName().equals(name))
                .findAny();
        if (pd.isPresent()) {
            return Optional.of(pd.get().getWriteMethod().getGenericParameterTypes()[0]);
        }
        return Optional.empty();
    }

    public void updateBeanDataConstructorArgs(BeanData data, Parameter[] parameters) {
        final List<BeanArg> args = Stream.of(parameters)
                .map(p -> {
                    final Optional<BeanArg> found = data.beanArgs.stream()
                            .filter(a -> a.name.isEqualTo(parameterName(p)).get())
                            .findFirst();
                    if (found.isPresent()) {
                        found.get().type.set(p.getType().getName());
                        return found.get();
                    } else {
                        final BeanArg arg = new BeanArg();
                        arg.name.set(parameterName(p));
                        arg.type.set(p.getType().getName());
                        return arg;
                    }
                })
                .collect(toList());
        data.beanArgs.clear();
        data.beanArgs.addAll(args);
    }

    public void updateBeanData(BeanData data) {
        final Class<?> type = getClass(data).orElse(null);
        if (type == null) {
            return;
        }
        final List<Executable> executables = getConstructors(data).collect(toList());
        if (!executables.isEmpty()) {
            if (executables.size() == 1) {
                updateBeanDataConstructorArgs(data, executables.get(0).getParameters());
            } else {
                final Optional<? extends Executable> executable = getConstructor(data);
                if (executable.isPresent()) {
                    updateBeanDataConstructorArgs(data, executable.get().getParameters());
                }
            }
        }

        final List<PropertyDescriptor> propertyDescriptors = getPropertyDescriptors(data).collect(toList());
        final Map<String, BeanProp> pmap = data.properties.stream().collect(toMap(e -> e.name.get(), e -> e));
        data.properties.clear();
        for (final PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            final BeanProp prop = pmap.computeIfAbsent(propertyDescriptor.getName(), n -> {
                final BeanProp property = new BeanProp();
                property.name.set(n);
                return property;
            });
            prop.type.set(propertyDescriptor.getPropertyType().getName());
            data.properties.add(prop);
        }
    }

    public Stream<PropertyDescriptor> getPropertyDescriptors(BeanData data) {
        final Class<?> type = getClass(data).orElse(Object.class);
        try {
            final BeanInfo beanInfo = Introspector.getBeanInfo(type);
            return Stream.of(beanInfo.getPropertyDescriptors())
                    .filter(d -> d.getWriteMethod() != null);
        } catch (IntrospectionException x) {
            return Stream.empty();
        }
    }
}
