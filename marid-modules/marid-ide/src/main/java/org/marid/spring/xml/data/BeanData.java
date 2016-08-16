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

import javax.xml.bind.annotation.*;
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
@XmlRootElement(name = "bean")
@XmlSeeAlso({BeanProp.class, BeanArg.class})
@XmlAccessorType(XmlAccessType.NONE)
public class BeanData implements AbstractData<BeanData> {

    public final StringProperty type = new SimpleStringProperty(this, "class");
    public final StringProperty name = new SimpleStringProperty(this, "name");
    public final StringProperty initMethod = new SimpleStringProperty(this, "init-method");
    public final StringProperty destroyMethod = new SimpleStringProperty(this, "destroy-method");
    public final StringProperty factoryBean = new SimpleStringProperty(this, "factory-bean");
    public final StringProperty factoryMethod = new SimpleStringProperty(this, "factory-method");
    public final StringProperty lazyInit = new SimpleStringProperty(this, "lazy-init");

    public final ObservableList<BeanArg> beanArgs = FXCollections.observableArrayList();
    public final ObservableList<BeanProp> properties = FXCollections.observableArrayList();

    @XmlAttribute(name = "class")
    public String getType() {
        return type.isEmpty().get() ? null : type.get();
    }

    public void setType(String type) {
        this.type.set(type);
    }

    @XmlAttribute(name = "name")
    public String getName() {
        return name.isEmpty().get() ? null : name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    @XmlAttribute(name = "init-method")
    public String getInitMethod() {
        return initMethod.isEmpty().get() ? null : initMethod.get();
    }

    public void setInitMethod(String initMethod) {
        this.initMethod.set(initMethod);
    }

    @XmlAttribute(name = "destroy-method")
    public String getDestroyMethod() {
        return destroyMethod.isEmpty().get() ? null : destroyMethod.get();
    }

    public void setDestroyMethod(String destroyMethod) {
        this.destroyMethod.set(destroyMethod);
    }

    @XmlAttribute(name = "factory-bean")
    public String getFactoryBean() {
        return factoryBean.isEmpty().get() ? null : factoryBean.get();
    }

    public void setFactoryBean(String factoryBean) {
        this.factoryBean.set(factoryBean);
    }

    @XmlAttribute(name = "factory-method")
    public String getFactoryMethod() {
        return factoryMethod.isEmpty().get() ? null : factoryMethod.get();
    }

    public void setFactoryMethod(String factoryMethod) {
        this.factoryMethod.set(factoryMethod);
    }

    @XmlAttribute(name = "lazy-init")
    public String getLazyInit() {
        return lazyInit.isEmpty().get() ? null : lazyInit.get();
    }

    public void setLazyInit(String lazyInit) {
        this.lazyInit.set(lazyInit);
    }

    @XmlElement(name = "constructor-arg")
    public BeanArg[] getBeanArgs() {
        return beanArgs.stream().filter(a -> !a.isEmpty()).toArray(BeanArg[]::new);
    }

    public void setBeanArgs(BeanArg[] beanArgs) {
        this.beanArgs.addAll(beanArgs);
    }

    @XmlElement(name = "property")
    public BeanProp[] getBeanProps() {
        return properties.stream().filter(p -> !p.isEmpty()).toArray(BeanProp[]::new);
    }

    public void setBeanProps(BeanProp[] beanProps) {
        this.properties.addAll(beanProps);
    }

    public boolean isFactoryBean() {
        return factoryBean.isNotEmpty().get() || factoryMethod.isNotEmpty().get();
    }

    public Optional<BeanProp> property(String name) {
        return properties.stream()
                .filter(p -> p.name.isEqualTo(name).get())
                .findAny();
    }

    public Stream<? extends Executable> getConstructors(ProjectProfile profile) {
        if (isFactoryBean()) {
            if (factoryBean.isNotEmpty().get()) {
                return profile.getBeanFiles().stream()
                        .flatMap(e -> e.getValue().allBeans())
                        .filter(b -> factoryBean.isEqualTo(b.nameProperty()).get())
                        .map(b -> b.getClass(profile))
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
        final List<? extends Executable> executables = getConstructors(profile).collect(toList());
        switch (executables.size()) {
            case 0:
                return Optional.empty();
            case 1:
                return Optional.of(executables.get(0));
            default:
                final Class<?>[] types = beanArgs.stream()
                        .map(a -> profile.getClass(a.type.get()).orElse(Object.class))
                        .toArray(Class<?>[]::new);
                return executables.stream().filter(m -> Arrays.equals(types, m.getParameterTypes())).findFirst();
        }
    }

    public Optional<Class<?>> getClass(ProjectProfile profile) {
        if (isFactoryBean()) {
            return getConstructor(profile).map(e -> ((Method) e).getReturnType());
        } else {
            return profile.getClass(type.get());
        }
    }

    public Optional<? extends Type> getType(ProjectProfile profile) {
        if (isFactoryBean()) {
            return getConstructor(profile).map(e -> ((Method) e).getGenericReturnType());
        } else {
            return profile.getClass(type.get());
        }
    }

    public Optional<? extends Type> getArgType(ProjectProfile profile, String name) {
        final Optional<? extends Executable> c = getConstructor(profile);
        if (c.isPresent()) {
            final Parameter[] parameters = c.get().getParameters();
            final Optional<Parameter> parameter = Stream.of(parameters).filter(p -> p.getName().equals(name)).findAny();
            if (parameter.isPresent()) {
                return Optional.of(parameter.get().getParameterizedType());
            }
        }
        return Optional.empty();
    }

    public Optional<? extends Type> getPropType(ProjectProfile profile, String name) {
        final Optional<PropertyDescriptor> pd = getPropertyDescriptors(profile)
                .filter(d -> d.getName().equals(name))
                .findAny();
        if (pd.isPresent()) {
            return Optional.of(pd.get().getWriteMethod().getGenericParameterTypes()[0]);
        }
        return Optional.empty();
    }

    public void updateBeanDataConstructorArgs(Parameter[] parameters) {
        final List<BeanArg> args = Stream.of(parameters)
                .map(p -> {
                    final Optional<BeanArg> found = beanArgs.stream()
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
        beanArgs.clear();
        beanArgs.addAll(args);
    }

    public void updateBeanData(ProjectProfile profile) {
        final Class<?> type = getClass(profile).orElse(null);
        if (type == null) {
            return;
        }
        final List<Executable> executables = getConstructors(profile).collect(toList());
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

        final List<PropertyDescriptor> propertyDescriptors = getPropertyDescriptors(profile).collect(toList());
        final Map<String, BeanProp> pmap = properties.stream().collect(toMap(e -> e.name.get(), e -> e));
        properties.clear();
        for (final PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            final BeanProp prop = pmap.computeIfAbsent(propertyDescriptor.getName(), n -> {
                final BeanProp property = new BeanProp();
                property.name.set(n);
                return property;
            });
            prop.type.set(propertyDescriptor.getPropertyType().getName());
            properties.add(prop);
        }
    }

    public Stream<PropertyDescriptor> getPropertyDescriptors(ProjectProfile profile) {
        final Class<?> type = getClass(profile).orElse(Object.class);
        try {
            final BeanInfo beanInfo = Introspector.getBeanInfo(type);
            return Stream.of(beanInfo.getPropertyDescriptors())
                    .filter(d -> d.getWriteMethod() != null);
        } catch (IntrospectionException x) {
            return Stream.empty();
        }
    }

    public StringProperty nameProperty() {
        return name;
    }

    public boolean isEmpty() {
        return (type.isEmpty().get() && factoryMethod.isEmpty().get()) || name.isEmpty().get();
    }
}
