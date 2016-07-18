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

package org.marid.spring.xml.data;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.marid.ide.project.ProjectProfile;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.stripToNull;
import static org.marid.misc.Reflections.parameterName;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanData extends AbstractData<BeanData> implements BeanLike {

    public final StringProperty type = new SimpleStringProperty(this, "class");
    public final StringProperty name = new SimpleStringProperty(this, "name");
    public final StringProperty initMethod = new SimpleStringProperty(this, "init-method");
    public final StringProperty destroyMethod = new SimpleStringProperty(this, "destroy-method");
    public final StringProperty factoryBean = new SimpleStringProperty(this, "factory-bean");
    public final StringProperty factoryMethod = new SimpleStringProperty(this, "factory-method");
    public final StringProperty lazyInit = new SimpleStringProperty(this, "lazy-init");

    public final ObservableList<ConstructorArg> constructorArgs = FXCollections.observableArrayList();
    public final ObservableList<Property> properties = FXCollections.observableArrayList();

    public boolean isFactoryBean() {
        return factoryBean.isNotEmpty().get() || factoryMethod.isNotEmpty().get();
    }

    public Optional<Property> property(String name) {
        return properties.stream()
                .filter(p -> p.name.isEqualTo(name).get())
                .findAny();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(defaultIfBlank(type.get(), ""));
        out.writeUTF(defaultIfBlank(name.get(), ""));
        out.writeUTF(defaultIfBlank(initMethod.get(), ""));
        out.writeUTF(defaultIfBlank(destroyMethod.get(), ""));
        out.writeUTF(defaultIfBlank(factoryBean.get(), ""));
        out.writeUTF(defaultIfBlank(factoryMethod.get(), ""));
        out.writeUTF(defaultIfBlank(lazyInit.get(), ""));

        out.writeInt(constructorArgs.size());
        for (final ConstructorArg arg : constructorArgs) {
            out.writeObject(arg);
        }

        out.writeInt(properties.size());
        for (final Property property : properties) {
            out.writeObject(property);
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        type.set(stripToNull(in.readUTF()));
        name.set(stripToNull(in.readUTF()));
        initMethod.set(stripToNull(in.readUTF()));
        destroyMethod.set(stripToNull(in.readUTF()));
        factoryBean.set(stripToNull(in.readUTF()));
        factoryMethod.set(stripToNull(in.readUTF()));
        lazyInit.set(stripToNull(in.readUTF()));

        final int argCount = in.readInt();
        for (int i = 0; i < argCount; i++) {
            constructorArgs.add((ConstructorArg) in.readObject());
        }

        final int propCount = in.readInt();
        for (int i = 0; i < propCount; i++) {
            properties.add((Property) in.readObject());
        }
    }

    @Override
    public Stream<? extends Executable> getConstructors(ProjectProfile profile) {
        if (isFactoryBean()) {
            if (factoryBean.isNotEmpty().get()) {
                return profile.getBeanFiles().values().stream()
                        .flatMap(f -> f.beans.stream())
                        .filter(b -> factoryBean.isEqualTo(b.name).get())
                        .map(b -> getClass(profile))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .flatMap(t -> Stream.of(t.getMethods()))
                        .filter(m -> m.getReturnType() != void.class)
                        .filter(m -> factoryMethod.isEqualTo(m.getName()).get())
                        .sorted(comparingInt(Method::getParameterCount));
            } else {
                return profile.getClass(type.get())
                        .map(type -> Stream.of(type.getMethods())
                                .filter(m -> Modifier.isStatic(m.getModifiers()))
                                .filter(m -> m.getReturnType() != void.class)
                                .filter(m -> factoryMethod.isEqualTo(m.getName()).get())
                                .sorted(comparingInt(Method::getParameterCount)))
                        .orElse(Stream.empty());
            }
        } else {
            return getClass(profile)
                    .map(c -> Stream.of(c.getConstructors()).sorted(comparingInt(Constructor::getParameterCount)))
                    .orElseGet(Stream::empty);
        }
    }

    public Optional<? extends Executable> getConstructor(ProjectProfile profile) {
        final List<? extends Executable> executables = getConstructors(profile).collect(Collectors.toList());
        switch (executables.size()) {
            case 0:
                return Optional.empty();
            case 1:
                return Optional.of(executables.get(0));
            default:
                final Class<?>[] types = constructorArgs.stream()
                        .map(a -> profile.getClass(a.type.get()).orElse(Object.class))
                        .toArray(Class<?>[]::new);
                return executables.stream().filter(m -> Arrays.equals(types, m.getParameterTypes())).findFirst();
        }
    }

    @Override
    public Optional<Class<?>> getClass(ProjectProfile profile) {
        if (isFactoryBean()) {
            return getConstructor(profile).map(e -> ((Method) e).getReturnType());
        } else {
            return profile.getClass(type.get());
        }
    }

    public void updateBeanDataConstructorArgs(Parameter[] parameters) {
        final List<ConstructorArg> args = Stream.of(parameters)
                .map(p -> {
                    final Optional<ConstructorArg> found = constructorArgs.stream()
                            .filter(a -> a.name.isEqualTo(parameterName(p)).get())
                            .findFirst();
                    if (found.isPresent()) {
                        found.get().type.set(p.getType().getName());
                        return found.get();
                    } else {
                        final ConstructorArg arg = new ConstructorArg();
                        arg.name.set(parameterName(p));
                        arg.type.set(p.getType().getName());
                        return arg;
                    }
                })
                .collect(Collectors.toList());
        constructorArgs.clear();
        constructorArgs.addAll(args);
    }

    public void updateBeanData(ProjectProfile profile) {
        final Class<?> type = getClass(profile).orElse(null);
        if (type == null) {
            return;
        }
        final List<Executable> executables = getConstructors(profile).collect(Collectors.toList());
        if (!executables.isEmpty()) {
            if (executables.size() == 1) {
                updateBeanDataConstructorArgs(executables.get(0).getParameters());
            } else {
                final Optional<? extends Executable> executable = getConstructor(profile);
                if (executable.isPresent()) {
                    updateBeanDataConstructorArgs(executable.get().getParameters());
                }
            }
        }

        final List<PropertyDescriptor> propertyDescriptors;
        try {
            propertyDescriptors = Stream.of(Introspector.getBeanInfo(type).getPropertyDescriptors())
                    .filter(d -> d.getWriteMethod() != null)
                    .collect(Collectors.toList());
        } catch (IntrospectionException x) {
            return;
        }
        final Map<String, Property> pmap = properties.stream().collect(toMap(e -> e.name.get(), e -> e));
        properties.clear();
        for (final PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            final Property prop = pmap.computeIfAbsent(propertyDescriptor.getName(), n -> {
                final Property property = new Property();
                property.name.set(n);
                return property;
            });
            prop.type.set(propertyDescriptor.getPropertyType().getName());
            properties.add(prop);
        }
    }

    @Override
    public StringProperty nameProperty() {
        return name;
    }
}
