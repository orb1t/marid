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

import com.google.common.collect.ImmutableMap;
import org.marid.spring.dependant.DependantConfiguration;
import org.marid.spring.postprocessors.LogBeansPostProcessor;
import org.marid.spring.postprocessors.OrderedInitPostProcessor;
import org.marid.spring.postprocessors.WindowAndDialogPostProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.marid.spring.dependant.DependantConfiguration.PARAM;

/**
 * @author Dmitry Ovchinnikov
 */
@Component("dependants")
public class IdeDependants {

    private static final List<AnnotationConfigApplicationContext> CONTEXTS = new CopyOnWriteArrayList<>();

    private final GenericApplicationContext parent;

    @Autowired
    public IdeDependants(GenericApplicationContext parent) {
        this.parent = parent;
    }

    public void start(Consumer<AnnotationConfigApplicationContext> consumer) {
        final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.addBeanFactoryPostProcessor(beanFactory -> {
            beanFactory.addBeanPostProcessor(new OrderedInitPostProcessor(context));
            beanFactory.addBeanPostProcessor(new LogBeansPostProcessor());
            beanFactory.addBeanPostProcessor(new WindowAndDialogPostProcessor(context));
            beanFactory.setParentBeanFactory(parent.getDefaultListableBeanFactory());
        });
        context.setAllowBeanDefinitionOverriding(false);
        context.setAllowCircularReferences(false);
        context.register(IdeDependants.class);
        context.addApplicationListener(e -> {
            if (e instanceof ApplicationContextEvent) {
                if (e instanceof ContextClosedEvent) {
                    CONTEXTS.remove(context);
                } else if (e instanceof ContextStartedEvent) {
                    CONTEXTS.add(context);
                }
            }
        });
        parent.addApplicationListener(event -> {
            if (event instanceof ContextClosedEvent) {
                parent.getApplicationListeners().remove(this);
                context.close();
            }
        });
        consumer.accept(context);
        context.refresh();
        context.start();
    }

    public final void start(Class<?> conf, Consumer<AnnotationConfigApplicationContext> consumer) {
        start(context -> {
            context.register(conf);
            consumer.accept(context);
        });
    }

    public <T> void start(Class<? extends DependantConfiguration<T>> conf, T param, Consumer<AnnotationConfigApplicationContext> consumer) {
        start(context -> {
            final Map<String, Object> map = ImmutableMap.of(PARAM, param);
            context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("paramMap", map));
            context.register(conf);
            consumer.accept(context);
        });
    }

    public static List<AnnotationConfigApplicationContext> getMatched(Object param) {
        return CONTEXTS.stream()
                .filter(c -> Objects.equals(param, c.getEnvironment().getProperty(PARAM, Object.class)))
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return parent.toString();
    }
}
