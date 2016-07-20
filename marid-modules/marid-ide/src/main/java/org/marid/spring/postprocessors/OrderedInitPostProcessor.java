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

package org.marid.spring.postprocessors;

import org.marid.spring.annotation.OrderedInit;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.MethodParameter;

import java.lang.reflect.Parameter;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * @author Dmitry Ovchinnikov
 */
public class OrderedInitPostProcessor implements BeanPostProcessor {

    private final AnnotationConfigApplicationContext context;

    public OrderedInitPostProcessor(AnnotationConfigApplicationContext context) {
        this.context = context;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        final DefaultListableBeanFactory f = new DefaultListableBeanFactory(context.getAutowireCapableBeanFactory());
        Stream.of(bean.getClass().getMethods())
                .filter(m -> m.isAnnotationPresent(OrderedInit.class))
                .sorted(Comparator.comparingInt(m -> m.getAnnotation(OrderedInit.class).value()))
                .forEachOrdered(method -> {
                    final boolean eager = !method.isAnnotationPresent(Lazy.class);
                    final Object[] args = new Object[method.getParameterCount()];
                    final Parameter[] parameters = method.getParameters();
                    for (int i = 0; i < args.length; i++) {
                        final MethodParameter methodParameter = new MethodParameter(method, i);
                        final Autowired autowired = parameters[i].getAnnotation(Autowired.class);
                        final boolean required = autowired != null && autowired.required();
                        final DependencyDescriptor descriptor = new DependencyDescriptor(methodParameter, required, eager);
                        try {
                            args[i] = f.resolveDependency(descriptor, null);
                        } catch (Exception x) {
                            throw new BeanInstantiationException(bean.getClass(), "Unable to autowire " + methodParameter, x);
                        }
                    }
                    try {
                        final Object result = method.invoke(bean, args);
                        if (result != null) {
                            f.registerSingleton(method.getName(), result);
                        }
                    } catch (Exception x) {
                        throw new BeanInstantiationException(bean.getClass(), null, x);
                    }
                });
        return bean;
    }
}
