/*-
 * #%L
 * marid-webapp
 * %%
 * Copyright (C) 2012 - 2018 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.marid.app.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.support.GenericApplicationContext;

import java.util.function.Consumer;

public interface ContextUtils {

  @SafeVarargs
  static AnnotationConfigApplicationContext context(GenericApplicationContext parent,
                                                    Consumer<AnnotationConfigApplicationContext>... configurers) {
    final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    context.setAllowBeanDefinitionOverriding(false);
    context.setAllowCircularReferences(false);
    context.getBeanFactory().addBeanPostProcessor(new LoggingPostProcessor());

    context.setParent(parent);

    final var parentListener = closeListener(parent, event -> {
      try {
        context.close();
      } catch (Exception x) {
        x.printStackTrace();
      }
    });
    parent.addApplicationListener(parentListener);

    final var listener = closeListener(context, e -> parent.getApplicationListeners().remove(parentListener));
    context.addApplicationListener(listener);

    for (final Consumer<AnnotationConfigApplicationContext> configurer : configurers) {
      configurer.accept(context);
    }

    return context;
  }

  static ApplicationListener<ContextClosedEvent> closeListener(ApplicationContext context,
                                                               ApplicationListener<ContextClosedEvent> listener) {
    return ev -> {
      if (ev.getApplicationContext() == context) {
        listener.onApplicationEvent(ev);
      }
    };
  }

  static ApplicationListener<ContextStartedEvent> startListener(ApplicationContext context,
                                                                ApplicationListener<ContextStartedEvent> listener) {
    return ev -> {
      if (ev.getApplicationContext() == context) {
        listener.onApplicationEvent(ev);
      }
    };
  }
}
