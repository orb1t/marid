/*
 * Copyright (c) 2017 Dmitry Ovchinnikov
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

package org.marid.spring.beans;

import org.marid.ide.project.ProjectProfile;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.ResolvableType;

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.springframework.core.ResolvableType.NONE;

/**
 * @author Dmitry Ovchinnikov
 */
public interface MaridBeanUtils {

    static Executable constructor(BeanDefinition definition, DefaultListableBeanFactory factory, ProjectProfile profile) {
        final String factoryMethodName = definition.getFactoryMethodName();
        final String className = definition.getBeanClassName();
        final ConstructorArgumentValues argumentValues = definition.getConstructorArgumentValues();
        final String[] argNames = argumentValues.getGenericArgumentValues().stream()
                .map(ValueHolder::getName)
                .sorted()
                .toArray(String[]::new);
        final Predicate<Executable> argMatcher = e -> Arrays.equals(argNames, Stream.of(e.getParameters())
                .map(Parameter::getName)
                .sorted()
                .toArray(String[]::new));
        if (factoryMethodName != null) {
            final ResolvableType factoryBeanType;
            if (definition.getFactoryBeanName() != null) {
                final BeanDefinition factoryBeanDefinition = factory.getBeanDefinition(definition.getFactoryBeanName());
                factoryBeanType = beanType(factoryBeanDefinition, factory, profile);
            } else {
                factoryBeanType = profile.getClass(className).map(ResolvableType::forClass).orElse(NONE);
            }
            if (factoryBeanType == NONE) {
                return null;
            } else {
                return Stream.of(factoryBeanType.getClass().getMethods())
                        .filter(m -> factoryMethodName.equals(m.getName()))
                        .filter(m -> m.getParameterCount() == argumentValues.getArgumentCount())
                        .filter(argMatcher)
                        .findAny()
                        .orElse(null);
            }
        } else {
            return profile.getClass(className)
                    .flatMap(cl -> Stream.of(cl.getConstructors())
                            .filter(c -> c.getParameterCount() == argumentValues.getArgumentCount())
                            .filter(argMatcher)
                            .findAny()
                    )
                    .orElse(null);
        }
    }

    static ResolvableType beanType(BeanDefinition definition, DefaultListableBeanFactory factory, ProjectProfile profile) {
        final String factoryMethodName = definition.getFactoryMethodName();
        final String className = definition.getBeanClassName();
        final ConstructorArgumentValues argumentValues = definition.getConstructorArgumentValues();
        final String[] argNames = argumentValues.getGenericArgumentValues().stream()
                .map(ValueHolder::getName)
                .sorted()
                .toArray(String[]::new);
        final Predicate<Executable> argMatcher = e -> Arrays.equals(argNames, Stream.of(e.getParameters())
                .map(Parameter::getName)
                .sorted()
                .toArray(String[]::new));
        if (factoryMethodName != null) {
            final ResolvableType factoryBeanType;
            if (definition.getFactoryBeanName() != null) {
                final BeanDefinition factoryBeanDefinition = factory.getBeanDefinition(definition.getFactoryBeanName());
                factoryBeanType = beanType(factoryBeanDefinition, factory, profile);
            } else {
                factoryBeanType = profile.getClass(className).map(ResolvableType::forClass).orElse(NONE);
            }
            if (factoryBeanType == NONE) {
                return NONE;
            } else {
                return Stream.of(factoryBeanType.getClass().getMethods())
                        .filter(m -> factoryMethodName.equals(m.getName()))
                        .filter(m -> m.getParameterCount() == argumentValues.getArgumentCount())
                        .filter(argMatcher)
                        .findAny()
                        .map(ResolvableType::forMethodReturnType)
                        .orElse(NONE);
            }
        } else {
            return profile.getClass(className).map(ResolvableType::forClass).orElse(NONE);
        }
    }
}
