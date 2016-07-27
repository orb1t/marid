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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toMap;
import static org.marid.misc.Reflections.parameterName;
import static org.marid.spring.xml.MaridBeanUtils.setAttr;

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

    public final ObservableList<BeanArg> beanArgs = FXCollections.observableArrayList();
    public final ObservableList<BeanProp> properties = FXCollections.observableArrayList();

    public boolean isFactoryBean() {
        return factoryBean.isNotEmpty().get() || factoryMethod.isNotEmpty().get();
    }

    public Optional<BeanProp> property(String name) {
        return properties.stream()
                .filter(p -> p.name.isEqualTo(name).get())
                .findAny();
    }

    @Override
    public Stream<? extends Executable> getConstructors(ProjectProfile profile) {
        if (isFactoryBean()) {
            if (factoryBean.isNotEmpty().get()) {
                return profile.getBeanFiles().values().stream()
                        .flatMap(BeanFile::allBeans)
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
        final List<? extends Executable> executables = getConstructors(profile).collect(Collectors.toList());
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

    @Override
    public Optional<Class<?>> getClass(ProjectProfile profile) {
        if (isFactoryBean()) {
            return getConstructor(profile).map(e -> ((Method) e).getReturnType());
        } else {
            return profile.getClass(type.get());
        }
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
                .collect(Collectors.toList());
        beanArgs.clear();
        beanArgs.addAll(args);
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

    @Override
    public StringProperty nameProperty() {
        return name;
    }

    @Override
    public void save(Node node, Document document) {
        final Element beanElement = document.createElement("bean");
        node.appendChild(beanElement);
        setAttr(name, beanElement);
        setAttr(destroyMethod, beanElement);
        setAttr(initMethod, beanElement);
        setAttr(factoryBean, beanElement);
        setAttr(factoryMethod, beanElement);
        setAttr(type, beanElement);
        setAttr(lazyInit, beanElement);

        beanArgs.forEach(beanArg -> beanArg.save(beanElement, document));
        properties.forEach(property -> property.save(beanElement, document));
    }

    @Override
    public void load(Node node, Document document) {
        {
            final Element e = (Element) node;
            name.set(e.getAttribute("name"));
            type.set(e.getAttribute("class"));
            lazyInit.set(e.getAttribute("lazy-init"));
            initMethod.set(e.getAttribute("init-method"));
            destroyMethod.set(e.getAttribute("destroy-method"));
            factoryBean.set(e.getAttribute("factory-bean"));
            factoryMethod.set(e.getAttribute("factory-method"));
        }
        final NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            final Node n = nodeList.item(i);
            if (!(n instanceof Element) || n.getNodeName() == null) {
                continue;
            }
            final Element e = (Element) n;
            switch (e.getNodeName()) {
                case "constructor-arg":
                    final BeanArg ca = new BeanArg();
                    ca.load(e, document);
                    beanArgs.add(ca);
                    break;
                case "property":
                    final BeanProp p = new BeanProp();
                    p.load(e, document);
                    properties.add(p);
                    break;
            }
        }
    }
}
