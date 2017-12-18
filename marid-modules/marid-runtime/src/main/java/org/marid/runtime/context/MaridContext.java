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

import org.marid.runtime.event.BeanPreDestroyEvent;
import org.marid.runtime.event.ContextStopEvent;
import org.marid.runtime.exception.MaridBeanNotFoundException;

import java.util.Properties;

/**
 * Runtime lite context.
 */
public class MaridContext implements MaridRuntime, AutoCloseable {

  private final BeanConfiguration configuration;
  private final MaridContext parent;
  private final String name;
  private final Object instance;
  private final MaridContext[] children;

  private MaridContext(BeanConfiguration configuration, MaridContext parent, BeanContext beanContext) {
    this.configuration = configuration;
    this.parent = parent;
    this.name = beanContext.getName();
    this.instance = beanContext.getInstance();
    this.children = beanContext.getChildren().stream()
        .map(c -> new MaridContext(configuration, this, c))
        .toArray(MaridContext[]::new);
  }

  public MaridContext(BeanConfiguration configuration, BeanContext beanContext) {
    this(configuration, null, beanContext);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public MaridContext getParent() {
    return parent;
  }

  @Override
  public Object getBean(String name) {
    if (parent == null) {
      throw new MaridBeanNotFoundException(name);
    } else {
      for (final MaridContext sibling : parent.children) {
        if (sibling != this && sibling.name.equals(name)) {
          return sibling.instance;
        }
      }
      if (parent.name.equals(name)) {
        return parent.instance;
      }
      return parent.getBean(name);
    }
  }

  @Override
  public ClassLoader getClassLoader() {
    return configuration.getPlaceholderResolver().getClassLoader();
  }

  @Override
  public String resolvePlaceholders(String value) {
    return configuration.getPlaceholderResolver().resolvePlaceholders(value);
  }

  @Override
  public Properties getApplicationProperties() {
    return configuration.getPlaceholderResolver().getProperties();
  }

  @Override
  public void close() {
    final IllegalStateException e = new IllegalStateException("Runtime close exception");
    try {
      for (int i = children.length - 1; i >= 0; i--) {
        final MaridContext child = children[i];
        try {
          child.close();
        } catch (Throwable x) {
          e.addSuppressed(x);
        }
      }
      final BeanPreDestroyEvent event = new BeanPreDestroyEvent(this, name, instance, e::addSuppressed);
      configuration.fireEvent(l -> l.onPreDestroy(event), e::addSuppressed);
      configuration.fireEvent(l -> l.onStop(new ContextStopEvent(this)), e::addSuppressed);
    } catch (Throwable x) {
      e.addSuppressed(x);
    } finally {
      if (parent != null) {
        for (int i = 0; i < parent.children.length; i++) {
          if (parent.children[i] == this) {
            parent.children[i] = null; // let GC to do its work
          }
        }
      }
    }
    if (e.getSuppressed().length > 0) {
      throw e;
    }
  }
}
