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
import org.marid.spring.xml.data.ConstructorArg;
import org.marid.spring.xml.data.Property;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class ProjectCacheManager implements LogSupport {

    private final ObjectFactory<ProjectMavenBuilder> projectBuilder;

    @Autowired
    public ProjectCacheManager(ObjectFactory<ProjectMavenBuilder> projectBuilder) {
        this.projectBuilder = projectBuilder;
    }

    public Class<?> getClass(ProjectProfile profile, String type) {
        return profile.cacheEntry.getClass(type);
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

    private Method getFactoryMethod(ProjectProfile profile, BeanData beanData) {
        final Class<?> beanType = profile.getBeanFiles().values().stream()
                .flatMap(f -> f.beans.stream())
                .filter(b -> beanData.factoryBean.isEqualTo(b.name).get())
                .map(b -> getClass(profile, b.type.get()))
                .findFirst()
                .orElse(null);
        if (beanType == null) {
            return null;
        }
        return Stream.of(beanType.getMethods())
                .filter(m -> beanData.factoryMethod.isEqualTo(m.getName()).get())
                .findFirst()
                .orElse(null);
    }

    public void updateBeanData(ProjectProfile profile, BeanData beanData) {
        final Class<?> type = getClass(profile, beanData.type.get());
        if (type == Object.class) {
            return;
        }

        ConstructorArgs:
        {
            final Parameter[] parameters;
            if (beanData.factoryBean.isNotEmpty().get() || beanData.factoryMethod.isNotEmpty().get()) {
                final Method method = getFactoryMethod(profile, beanData);
                if (method == null) {
                    break ConstructorArgs;
                }
                beanData.type.set(method.getReturnType().getName());
                parameters = method.getParameters();
            } else {
                final Constructor<?>[] constructors = type.getConstructors();
                if (constructors.length == 0) {
                    break ConstructorArgs;
                }
                parameters = constructors[0].getParameters();
            }
            final Map<String, ConstructorArg> map = beanData.constructorArgs.stream().collect(toMap(e -> e.name.get(), e -> e));
            beanData.constructorArgs.clear();
            for (final Parameter parameter : parameters) {
                final ConstructorArg arg = map.computeIfAbsent(parameter.getName(), n -> {
                    final ConstructorArg constructorArg = new ConstructorArg();
                    constructorArg.name.set(n);
                    return constructorArg;
                });
                arg.type.set(parameter.getType().getName());
                beanData.constructorArgs.add(arg);
            }
        }

        final Class<?> beanType = getClass(profile, beanData.type.get());
        if (beanType == Object.class) {
            return;
        }
        final List<PropertyDescriptor> propertyDescriptors;
        try {
            propertyDescriptors = Stream.of(Introspector.getBeanInfo(beanType).getPropertyDescriptors())
                    .filter(d -> d.getWriteMethod() != null)
                    .collect(Collectors.toList());
        } catch (IntrospectionException x) {
            return;
        }
        final Map<String, Property> map = beanData.properties.stream().collect(toMap(e -> e.name.get(), e -> e));
        beanData.properties.clear();
        for (final PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            final Property prop = map.computeIfAbsent(propertyDescriptor.getName(), n -> {
                final Property property = new Property();
                property.name.set(n);
                return property;
            });
            prop.type.set(propertyDescriptor.getPropertyType().getName());
            beanData.properties.add(prop);
        }
    }
}
