/*-
 * #%L
 * marid-runtime
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
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

package org.marid.runtime.context;

import org.jetbrains.annotations.NotNull;
import org.marid.runtime.event.*;
import org.marid.runtime.exception.MaridBeanInitializationException;
import org.marid.types.Classes;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.lang.System.getProperties;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridDefaultContextListener implements MaridContextListener {

  @Override
  public void bootstrap(@NotNull ContextBootstrapEvent contextBootstrapEvent) {
    final MaridRuntime runtime = contextBootstrapEvent.getSource();
    for (final String key : runtime.getApplicationProperties().stringPropertyNames()) {
      if (key.startsWith("system.")) {
        getProperties().setProperty(key.substring(7), runtime.getApplicationProperties().getProperty(key));
      }
    }
  }

  @Override
  public void onPostConstruct(@NotNull BeanPostConstructEvent postConstructEvent) {
    if (postConstructEvent.getBean() == null) {
      return;
    }
    final LinkedList<Method> methods = getMethods(postConstructEvent.getBean().getClass(), this::isPostConstruct, true);
    final HashSet<String> passed = new HashSet<>();
    for (final Method method : methods) {
      if (passed.add(method.getName())) {
        try {
          method.invoke(postConstructEvent.getBean());
        } catch (Throwable x) {
          throw new MaridBeanInitializationException(postConstructEvent.getName(), x);
        }
      }
    }
  }

  @Override
  public void onPreDestroy(@NotNull BeanPreDestroyEvent preDestroyEvent) {
    if (preDestroyEvent.getBean() == null) {
      return;
    }
    final LinkedList<Method> methods = getMethods(preDestroyEvent.getBean().getClass(), this::isPreDestroy, false);
    final HashSet<String> passed = new HashSet<>();
    for (final Method method : methods) {
      if (passed.add(method.getName())) {
        try {
          method.invoke(preDestroyEvent.getBean());
        } catch (Throwable x) {
          preDestroyEvent.getExceptionConsumer().accept(x);
        }
      }
    }
    if (preDestroyEvent.getBean() instanceof AutoCloseable) {
      try {
        ((AutoCloseable) preDestroyEvent.getBean()).close();
      } catch (Throwable x) {
        preDestroyEvent.getExceptionConsumer().accept(x);
      }
    }
  }

  @Override
  public void onStart(@NotNull ContextStartEvent contextStartEvent) {
  }

  @Override
  public void onStop(@NotNull ContextStopEvent contextStopEvent) {
  }

  @Override
  public void onFail(@NotNull ContextFailEvent contextFailEvent) {
  }

  @Override
  public int getOrder() {
    return Integer.MAX_VALUE - 100;
  }

  private LinkedList<Method> getMethods(Class<?> type, Predicate<Method> filter, boolean reversed) {
    return Classes.classes(type, false)
        .flatMap(t -> Stream.of(t.getDeclaredMethods()))
        .filter(filter)
        .filter(m -> m.isDefault() || !Modifier.isStatic(m.getModifiers()) && !Modifier.isAbstract(m.getModifiers()))
        .distinct()
        .filter(m -> {
          try {
            m.setAccessible(true);
            return true;
          } catch (Throwable x) {
            return false;
          }
        })
        .reduce(new LinkedList<>(), (a, e) -> {
          if (reversed) {
            a.addFirst(e);
          } else {
            a.addLast(e);
          }
          return a;
        }, (a1, a2) -> a2);
  }

  private boolean isPostConstruct(Method method) {
    return method.getParameterCount() == 0 && Stream.of(method.getAnnotations())
        .anyMatch(a -> a.annotationType().getName().equals("javax.annotation.PostConstruct"));
  }

  private boolean isPreDestroy(Method method) {
    return method.getParameterCount() == 0 && Stream.of(method.getAnnotations())
        .anyMatch(a -> a.annotationType().getName().equals("javax.annotation.PreDestroy"));
  }
}
