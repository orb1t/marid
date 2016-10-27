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

package org.marid;

import org.marid.spring.postprocessors.LogBeansPostProcessor;
import org.marid.spring.postprocessors.OrderedInitPostProcessor;
import org.marid.spring.postprocessors.WindowAndDialogPostProcessor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.CglibSubclassingInstantiationStrategy;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * @author Dmitry Ovchinnikov
 */
@Component("dependants")
public class IdeDependants {

    private final AnnotationConfigApplicationContext parent;

    @Autowired
    public IdeDependants(AnnotationConfigApplicationContext parent) {
        this.parent = parent;
    }

    @SafeVarargs
    public final AnnotationConfigApplicationContext start(Class<?> configuration,
                                                          String name,
                                                          Consumer<AnnotationConfigApplicationContext>... consumers) {
        final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        final AtomicReference<ApplicationListener<?>> listenerRef = new AtomicReference<>();
        listenerRef.set(event -> {
            if (event instanceof ContextClosedEvent) {
                final ContextClosedEvent contextClosedEvent = (ContextClosedEvent) event;
                if (contextClosedEvent.getApplicationContext() == parent) {
                    parent.getApplicationListeners().remove(listenerRef.get());
                    context.close();
                }
            }
        });
        context.getBeanFactory().addBeanPostProcessor(new OrderedInitPostProcessor(context));
        context.getBeanFactory().addBeanPostProcessor(new LogBeansPostProcessor());
        context.getBeanFactory().addBeanPostProcessor(new WindowAndDialogPostProcessor(context));
        context.register(IdeDependants.class, configuration);
        context.setParent(parent);
        context.setDisplayName(name);
        parent.addApplicationListener(listenerRef.get());
        for (final Consumer<AnnotationConfigApplicationContext> contextConsumer : consumers) {
            contextConsumer.accept(context);
        }
        context.refresh();
        context.start();
        return context;
    }

    @SafeVarargs
    public final <T> AnnotationConfigApplicationContext start(String name,
                                                              Class<T> configuration,
                                                              Consumer<T> configurationConsumer,
                                                              Consumer<AnnotationConfigApplicationContext>... consumers) {
        return start(configuration, name, context -> {
            final CglibSubclassingInstantiationStrategy is = new CglibSubclassingInstantiationStrategy() {
                @Override
                public Object instantiate(RootBeanDefinition bd, String beanName, BeanFactory owner) {
                    final Object object = super.instantiate(bd, beanName, owner);
                    if (configuration.isInstance(object)) {
                        configurationConsumer.accept(configuration.cast(object));
                    }
                    return object;
                }

                @Override
                public Object instantiate(RootBeanDefinition bd, String beanName, BeanFactory owner, Constructor<?> ctor, Object... args) {
                    final Object object = super.instantiate(bd, beanName, owner, ctor, args);
                    if (configuration.isInstance(object)) {
                        configurationConsumer.accept(configuration.cast(object));
                    }
                    return object;
                }
            };
            ((DefaultListableBeanFactory) context.getBeanFactory()).setInstantiationStrategy(is);
            for (final Consumer<AnnotationConfigApplicationContext> contextConsumer : consumers) {
                contextConsumer.accept(context);
            }
        });
    }

    @Override
    public String toString() {
        return parent.toString();
    }
}
