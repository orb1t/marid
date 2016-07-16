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

import org.marid.logging.LogSupport;
import org.marid.spring.xml.data.BeanData;
import org.marid.spring.xml.data.BeanFile;
import org.marid.spring.xml.data.ConstructorArg;
import org.marid.spring.xml.data.Property;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.net.URLClassLoader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;
import static org.marid.misc.Reflections.parameterName;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class ProjectCacheManager implements LogSupport {

    private final ObjectProvider<ProjectMavenBuilder> projectBuilder;

    @Autowired
    public ProjectCacheManager(ObjectProvider<ProjectMavenBuilder> projectBuilder) {
        this.projectBuilder = projectBuilder;
    }

    public URLClassLoader getClassLoader(ProjectProfile profile) {
        return profile.cacheEntry.getClassLoader();
    }

    public void build(ProjectProfile profile) {
        projectBuilder.getObject().build(profile, result -> {
            try {
                log(INFO, "[{0}] Built {1}", profile, result);
                profile.cacheEntry.update();
                log(INFO, "[{0}] Updated", profile);
            } catch (Exception x) {
                log(WARNING, "Unable to update cache {0}", x, profile);
            }
        }, profile.logger()::log);
    }

    public Stream<? extends Executable> getConstructors(ProjectProfile profile, BeanData beanData) {
        if (beanData.isFactoryBean()) {
            if (beanData.factoryBean.isNotEmpty().get()) {
                return profile.getBeanFiles().values().stream()
                        .flatMap(f -> f.beans.stream())
                        .filter(b -> beanData.factoryBean.isEqualTo(b.name).get())
                        .map(b -> getBeanClass(profile, b))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .flatMap(t -> Stream.of(t.getMethods()))
                        .filter(m -> m.getReturnType() != void.class)
                        .filter(m -> beanData.factoryMethod.isEqualTo(m.getName()).get())
                        .sorted(Comparator.comparingInt(Method::getParameterCount));
            } else {
                return profile.getClass(beanData.type.get())
                        .map(type -> Stream.of(type.getMethods())
                                .filter(m -> Modifier.isStatic(m.getModifiers()))
                                .filter(m -> m.getReturnType() != void.class)
                                .filter(m -> beanData.factoryMethod.isEqualTo(m.getName()).get())
                                .sorted(Comparator.comparingInt(Method::getParameterCount)))
                        .orElse(Stream.empty());
            }
        } else {
            return getBeanClass(profile, beanData)
                    .map(c -> Stream.of(c.getConstructors()))
                    .orElseGet(Stream::empty);
        }
    }

    private Optional<? extends Executable> getConstructor(ProjectProfile profile, BeanData beanData) {
        final Class<?>[] types = beanData.constructorArgs.stream()
                .map(a -> profile.getClass(a.type.get()).orElse(Object.class))
                .toArray(Class<?>[]::new);
        return getConstructors(profile, beanData)
                .filter(m -> Arrays.equals(types, m.getParameterTypes()))
                .findFirst();
    }

    public Optional<Class<?>> getBeanClass(ProjectProfile profile, BeanData beanData) {
        if (beanData.isFactoryBean()) {
            return getConstructor(profile, beanData).map(e -> ((Method) e).getReturnType());
        } else {
            return profile.cacheEntry.getClass(beanData.type.get());
        }
    }

    public void updateBeanDataConstructorArgs(Parameter[] parameters, BeanData beanData) {
        final List<ConstructorArg> args = Stream.of(parameters)
                .map(p -> {
                    final Optional<ConstructorArg> found = beanData.constructorArgs.stream()
                            .filter(a -> a.name.isEqualTo(parameterName(p)).get())
                            .filter(a -> a.type.isEqualTo(p.getType().getName()).get())
                            .findFirst();
                    if (found.isPresent()) {
                        return found.get();
                    } else {
                        final ConstructorArg arg = new ConstructorArg();
                        arg.name.set(parameterName(p));
                        arg.type.set(p.getType().getName());
                        return arg;
                    }
                })
                .collect(Collectors.toList());
        beanData.constructorArgs.clear();
        beanData.constructorArgs.addAll(args);
    }

    public void updateBeanData(ProjectProfile profile, BeanData beanData) {
        final Class<?> type = getBeanClass(profile, beanData).orElseThrow(IllegalStateException::new);
        final Parameter[] parameters = getConstructor(profile, beanData)
                .map(Executable::getParameters)
                .orElseGet(() -> new Parameter[0]);
        if (parameters.length > 0) {
            updateBeanDataConstructorArgs(parameters, beanData);
        }

        final List<PropertyDescriptor> propertyDescriptors;
        try {
            propertyDescriptors = Stream.of(Introspector.getBeanInfo(type).getPropertyDescriptors())
                    .filter(d -> d.getWriteMethod() != null)
                    .collect(Collectors.toList());
        } catch (IntrospectionException x) {
            return;
        }
        final Map<String, Property> pmap = beanData.properties.stream().collect(toMap(e -> e.name.get(), e -> e));
        beanData.properties.clear();
        for (final PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            final Property prop = pmap.computeIfAbsent(propertyDescriptor.getName(), n -> {
                final Property property = new Property();
                property.name.set(n);
                return property;
            });
            prop.type.set(propertyDescriptor.getPropertyType().getName());
            beanData.properties.add(prop);
        }
    }

    public boolean containsBean(ProjectProfile profile, String name) {
        for (final BeanFile file : profile.beanFiles.values()) {
            for (final BeanData beanData : file.beans) {
                if (beanData.name.isEqualTo(name).get()) {
                    return true;
                }
            }
        }
        return false;
    }

    public String generateBeanName(ProjectProfile profile, String name) {
        while (containsBean(profile, name)) {
            name += "_new";
        }
        return name;
    }
}
