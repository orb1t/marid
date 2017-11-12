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

import org.marid.runtime.event.*;
import org.marid.runtime.exception.MaridBeanInitializationException;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.stream.Stream;

import static java.lang.System.getProperties;
import static org.marid.runtime.context.MaridRuntimeUtils.methods;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridDefaultContextListener implements MaridContextListener {

  @Override
  public void bootstrap(@Nonnull ContextBootstrapEvent contextBootstrapEvent) {
    final MaridRuntime runtime = contextBootstrapEvent.getSource();
    for (final String key : runtime.getApplicationProperties().stringPropertyNames()) {
      if (key.startsWith("system.")) {
        getProperties().setProperty(key.substring(7), runtime.getApplicationProperties().getProperty(key));
      }
    }
  }

  @Override
  public void onEvent(@Nonnull BeanEvent event) {
  }

  @Override
  public void onPostConstruct(@Nonnull BeanPostConstructEvent postConstructEvent) {
    if (postConstructEvent.getBean() == null) {
      return;
    }
    final Comparator<Method> byClass = this::cmp;
    final Comparator<Method> cmp = byClass.thenComparing(Method::getName);
    final TreeSet<Method> methods = methods(postConstructEvent.getBean(), this::isPostConstruct, cmp);
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
  public void onPreDestroy(@Nonnull BeanPreDestroyEvent preDestroyEvent) {
    if (preDestroyEvent.getBean() == null) {
      return;
    }
    final Comparator<Method> byClass = this::cmp;
    final Comparator<Method> cmp = byClass.thenComparing(Method::getName).reversed();
    final TreeSet<Method> methods = methods(preDestroyEvent.getBean(), this::isPreDestroy, cmp);
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
  public void onStart(@Nonnull ContextStartEvent contextStartEvent) {
  }

  @Override
  public void onStop(@Nonnull ContextStopEvent contextStopEvent) {
  }

  @Override
  public void onFail(@Nonnull ContextFailEvent contextFailEvent) {
  }

  @Override
  public int getOrder() {
    return Integer.MAX_VALUE - 100;
  }

  private int cmp(Method m1, Method m2) {
    if (m1.getDeclaringClass().equals(m2.getDeclaringClass())) {
      return 0;
    } else if (m1.getDeclaringClass().isAssignableFrom(m2.getDeclaringClass())) {
      return -1;
    } else {
      return 1;
    }
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
