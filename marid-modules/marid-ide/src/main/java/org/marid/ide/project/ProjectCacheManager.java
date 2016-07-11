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
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

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

    public Optional<Class<?>> getBeanClass(ProjectProfile profile, BeanData beanData) {
        if (beanData.isFactoryBean()) {
            final Optional<Class<?>> type = profile.getBeanFiles().values().stream()
                    .flatMap(f -> f.beans.stream())
                    .filter(b -> beanData.factoryBean.isEqualTo(b.name).get())
                    .map(b -> getBeanClass(profile, b))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .flatMap(c -> Stream.of(c.getMethods()))
                    .filter(m -> m.getName().equals(beanData.factoryMethod.get()))
                    .reduce((m1, m2) -> m2.getParameterCount() < m1.getParameterCount() ? m2 : m1)
                    .map(Method::getReturnType);
            if (type.isPresent()) {
                if (beanData.type.isNotEqualTo(type.get().getName()).get()) {
                    beanData.type.set(type.get().getName());
                }
                return type;
            }
        }
        return profile.cacheEntry.getClass(beanData.type.get());
    }

    public void updateBeanData(ProjectProfile profile, BeanData beanData) {
        try {
            final Class<?> type = getBeanClass(profile, beanData).orElseThrow(IllegalStateException::new);
            final Parameter[] parameters;
            if (beanData.isFactoryBean()) {
                parameters = profile.getBeanFiles().values().stream()
                        .flatMap(f -> f.beans.stream())
                        .filter(b -> beanData.factoryBean.isEqualTo(b.name).get())
                        .map(b -> getBeanClass(profile, b))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .findFirst()
                        .flatMap(t -> Stream.of(t.getMethods())
                                .filter(m -> beanData.factoryMethod.isEqualTo(m.getName()).get())
                                .reduce((m1, m2) -> m2.getParameterCount() < m1.getParameterCount() ? m2 : m1))
                        .orElseThrow(IllegalStateException::new)
                        .getParameters();
            } else {
                parameters = Stream.of(type.getConstructors())
                        .reduce((c1, c2) -> c2.getParameterCount() < c1.getParameterCount() ? c2 : c1)
                        .orElseThrow(IllegalStateException::new)
                        .getParameters();
            }
            final Map<String, ConstructorArg> cmap = beanData.constructorArgs.stream().collect(toMap(e -> e.name.get(), e -> e));
            beanData.constructorArgs.clear();
            for (final Parameter parameter : parameters) {
                final ConstructorArg arg = cmap.computeIfAbsent(parameter.getName(), n -> {
                    final ConstructorArg constructorArg = new ConstructorArg();
                    constructorArg.name.set(n);
                    return constructorArg;
                });
                arg.type.set(parameter.getType().getName());
                beanData.constructorArgs.add(arg);
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
        } catch (IllegalStateException x) {
            // no op
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
