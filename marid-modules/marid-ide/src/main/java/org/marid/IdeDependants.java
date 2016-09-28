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
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.env.MapPropertySource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.logging.Level;

import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
@Component("dependants")
public class IdeDependants {

    private static final List<AnnotationConfigApplicationContext> CONTEXTS = new CopyOnWriteArrayList<>();

    private final AnnotationConfigApplicationContext parent;

    @Autowired
    public IdeDependants(AnnotationConfigApplicationContext parent) {
        this.parent = parent;
    }

    public AnnotationConfigApplicationContext start(String name, Consumer<Builder> builderConsumer) {
        final Builder builder = new Builder(name);
        builderConsumer.accept(builder);
        builder.initArgs();
        builder.context.refresh();
        builder.context.start();
        return builder.context;
    }

    @Override
    public String toString() {
        return parent.toString();
    }

    public void closeDependants() {
        for (final AnnotationConfigApplicationContext context : CONTEXTS) {
            try (final AnnotationConfigApplicationContext c = context) {
                log(Level.INFO, "Closing {0}", c);
            } catch (Exception x) {
                log(Level.WARNING, "Unable to close context", x);
            }
        }
        CONTEXTS.clear();
    }

    public final class Builder {

        private final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        private final Map<String, Object> args = new HashMap<>();

        private Builder(String name) {
            CONTEXTS.add(context);
            context.addApplicationListener(event -> {
                if (event instanceof ContextClosedEvent) {
                    final ContextClosedEvent contextClosedEvent = (ContextClosedEvent) event;
                    if (contextClosedEvent.getApplicationContext() == context) {
                        CONTEXTS.remove(context);
                    }
                }
            });
            context.getBeanFactory().addBeanPostProcessor(new OrderedInitPostProcessor(context));
            context.getBeanFactory().addBeanPostProcessor(new LogBeansPostProcessor());
            context.getBeanFactory().addBeanPostProcessor(new WindowAndDialogPostProcessor(context));
            context.register(IdeDependants.class);
            context.setParent(parent);
            context.setDisplayName(name);
        }

        public Builder conf(Class<?>... classes) {
            context.register(classes);
            return this;
        }

        public Builder withContext(Consumer<AnnotationConfigApplicationContext> contextConsumer) {
            contextConsumer.accept(context);
            return this;
        }

        public Builder arg(String name, Object arg) {
            args.put(name, arg);
            return this;
        }

        private void initArgs() {
            if (!args.isEmpty()) {
                context.getEnvironment().getPropertySources().addLast(new MapPropertySource("args", args));
            }
        }
    }
}
