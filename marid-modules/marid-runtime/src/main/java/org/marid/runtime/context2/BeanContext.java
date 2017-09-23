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

package org.marid.runtime.context2;

import org.marid.runtime.context.MaridRuntime;
import org.marid.runtime.event.BeanPostConstructEvent;
import org.marid.runtime.event.BeanPreDestroyEvent;
import org.marid.runtime.event.ContextBootstrapEvent;
import org.marid.runtime.event.ContextFailEvent;
import org.marid.runtime.exception.MaridBeanInitializationException;
import org.marid.runtime.exception.MaridBeanNotFoundException;
import org.marid.runtime.expression.Expression;
import org.marid.runtime.model.MaridBean;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;

import static java.util.logging.Level.SEVERE;
import static org.marid.logging.Log.log;

public final class BeanContext implements MaridRuntime, AutoCloseable {

    private final BeanContext parent;
    private final BeanContextConfiguration configuration;
    private final MaridBean bean;
    private final Object instance;
    private final ArrayList<BeanContext> children = new ArrayList<>();
    private final HashSet<MaridBean> processing = new HashSet<>();

    public BeanContext(@Nullable BeanContext parent,
                       @Nonnull BeanContextConfiguration configuration,
                       @Nonnull MaridBean bean) {
        this.parent = parent;
        this.configuration = configuration;
        this.bean = bean;

        configuration.fireEvent(false, l -> l.bootstrap(new ContextBootstrapEvent(this)));

        try {
            this.instance = parent == null ? null : parent.create(bean);
            for (final Expression initializer : bean.getInitializers()) {
                try {
                    initializer.execute(this);
                } catch (Throwable x) {
                    throw new MaridBeanInitializationException(bean.getName(), x);
                }
            }
            configuration.fireEvent(false, l -> l.onPostConstruct(new BeanPostConstructEvent(this, bean.getName(), instance)));

            for (final MaridBean child : bean.getChildren()) {
                if (children.stream().noneMatch(b -> b.bean.getName().equals(child.getName()))) {
                    children.add(new BeanContext(this, configuration, child));
                }
            }
        } catch (Throwable x) {
            configuration.fireEvent(false, l -> l.onFail(new ContextFailEvent(this, bean.getName(), x)));

            close();

            throw x;
        }
    }

    public BeanContext(BeanContextConfiguration configuration, MaridBean root) {
        this(null, configuration, root);
    }

    @Override
    public BeanContext getParent() {
        return parent;
    }

    public Object getInstance() {
        return instance;
    }

    @Override
    public Object getBean(String name) {
        return getAscendant(name);
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
    public Object getAscendant(String name) {
        if (parent == null) {
            throw new MaridBeanNotFoundException(name);
        } else {
            for (final MaridBean brother : parent.bean.getChildren()) {
                if (brother.getName().equals(name)) {
                    try {
                        return parent.children.stream()
                                .filter(c -> c.bean == brother)
                                .findFirst()
                                .map(c -> c.instance)
                                .orElseGet(() -> {
                                    final BeanContext c = new BeanContext(parent, configuration, brother);
                                    parent.children.add(c);
                                    return c.instance;
                                });
                    } catch (CircularBeanException x) {
                        // continue
                    }
                }
            }
            return parent.getAscendant(name);
        }
    }

    @Override
    public Object getDescendant(String name) {
        return bean.getChildren().stream()
                .filter(child -> child.getName().equals(name))
                .map(child -> children.stream()
                        .filter(c -> c.bean == child)
                        .findFirst()
                        .map(c -> c.instance)
                        .orElseGet(() -> {
                            final BeanContext c = new BeanContext(this, configuration, child);
                            children.add(c);
                            return c.instance;
                        }))
                .findFirst()
                .orElseThrow(() -> new MaridBeanNotFoundException(name));
    }

    private Object create(MaridBean bean) {
        if (processing.add(bean)) {
            try {
                return bean.getFactory().execute(this);
            } finally {
                processing.remove(bean);
            }
        } else {
            throw new CircularBeanException();
        }
    }

    @Override
    public void close() {
        final IllegalStateException e = new IllegalStateException("Runtime close exception");
        configuration.fireEvent(true, l -> l.onPreDestroy(new BeanPreDestroyEvent(this, bean.getName(), instance, e::addSuppressed)));
        for (int i = children.size() - 1; i >= 0; i--) {
            final BeanContext child = children.get(i);
            try {
                child.close();
            } catch (Throwable x) {
                e.addSuppressed(x);
            }
        }
        if (e.getSuppressed().length > 0) {
            log(SEVERE, "Unable to close {0}", e, this);
        }
        if (parent != null) {
            parent.close();
        }
    }

    private static final class CircularBeanException extends RuntimeException {

        private CircularBeanException() {
            super(null, null, false, false);
        }
    }
}
