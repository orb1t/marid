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
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.logging.Level;

import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
@Component("dependants")
public class IdeDependants {

    private static final LinkedList<AnnotationConfigApplicationContext> CONTEXTS = new LinkedList<>();

    private final AnnotationConfigApplicationContext parent;

    @Autowired
    public IdeDependants(AnnotationConfigApplicationContext parent) {
        this.parent = parent;
    }

    public final AnnotationConfigApplicationContext start(Class<?> configuration, Object... args) {
        return start(context -> {
            context.setDisplayName(configuration.getSimpleName());
            if (args.length > 0) {
                final AnnotatedGenericBeanDefinition definition = new AnnotatedGenericBeanDefinition(configuration);
                final ConstructorArgumentValues values = new ConstructorArgumentValues();
                for (final Object arg : args) {
                    values.addGenericArgumentValue(arg);
                }
                definition.setConstructorArgumentValues(values);
                definition.setScope(AbstractBeanFactory.SCOPE_SINGLETON);
                final String beanName = BeanDefinitionReaderUtils.generateBeanName(definition, context);
                AnnotationConfigUtils.processCommonDefinitionAnnotations(definition);
                final BeanDefinitionHolder holder = new BeanDefinitionHolder(definition, beanName);
                BeanDefinitionReaderUtils.registerBeanDefinition(holder, context);
            } else {
                context.register(configuration);
            }
        });
    }

    private AnnotationConfigApplicationContext start(Consumer<AnnotationConfigApplicationContext> contextConsumer) {
        final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        CONTEXTS.add(context);
        context.getBeanFactory().addBeanPostProcessor(new OrderedInitPostProcessor(context));
        context.getBeanFactory().addBeanPostProcessor(new LogBeansPostProcessor());
        context.register(IdeDependants.class);
        context.setParent(parent);
        contextConsumer.accept(context);
        context.refresh();
        context.start();
        return context;
    }

    public static void closeDependants() {
        for (final Iterator<AnnotationConfigApplicationContext> iterator = CONTEXTS.descendingIterator(); iterator.hasNext(); ) {
            try (final AnnotationConfigApplicationContext context = iterator.next()) {
                log(Level.INFO, "Closing {0}", context);
            } catch (Exception x) {
                log(Level.WARNING, "Unable to close context", x);
            } finally {
                iterator.remove();
            }
        }
    }
}
