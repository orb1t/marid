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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;
import java.util.stream.Stream;

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
    public final void start(String name, Consumer<AnnotationConfigApplicationContext>... consumers) {
        final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.getBeanFactory().addBeanPostProcessor(new OrderedInitPostProcessor(context));
        context.getBeanFactory().addBeanPostProcessor(new LogBeansPostProcessor());
        context.getBeanFactory().addBeanPostProcessor(new WindowAndDialogPostProcessor(context));
        context.register(IdeDependants.class);
        context.setParent(parent);
        context.setDisplayName(name);
        parent.addApplicationListener(new ApplicationListener<ContextClosedEvent>() {
            @Override
            public void onApplicationEvent(ContextClosedEvent event) {
                if (event.getApplicationContext() == parent) {
                    parent.getApplicationListeners().remove(this);
                    context.close();
                }
            }
        });
        Stream.of(consumers).forEach(c -> c.accept(context));
        context.refresh();
        context.start();
    }

    @SafeVarargs
    public final void start(Class<?> conf, String name, Consumer<AnnotationConfigApplicationContext>... consumers) {
        start(name, context -> {
            context.register(conf);
            Stream.of(consumers).forEach(c -> c.accept(context));
        });
    }

    @Override
    public String toString() {
        return parent.toString();
    }
}
