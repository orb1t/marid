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

import org.marid.beans.MaridBean;
import org.marid.beans.RuntimeBean;
import org.marid.runtime.event.BeanPostConstructEvent;
import org.marid.runtime.event.BeanPreDestroyEvent;
import org.marid.runtime.event.ContextBootstrapEvent;
import org.marid.runtime.event.ContextFailEvent;
import org.marid.runtime.exception.MaridBeanNotFoundException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;

public final class BeanContext implements MaridRuntime, AutoCloseable {

    private final BeanContext parent;
    private final BeanConfiguration configuration;
    private final RuntimeBean bean;
    private final Object instance;
    private final ConcurrentLinkedDeque<BeanContext> children = new ConcurrentLinkedDeque<>();
    private final HashSet<MaridBean> processing = new HashSet<>();

    public BeanContext(@Nullable BeanContext parent, @Nonnull BeanConfiguration configuration, @Nonnull RuntimeBean bean) {
        this.parent = parent;
        this.configuration = configuration;
        this.bean = bean;

        try {
            configuration.fireEvent(l -> l.bootstrap(new ContextBootstrapEvent(this)), null);

            this.instance = parent == null ? null : parent.create(bean, this);
            configuration.fireEvent(l -> l.onPostConstruct(new BeanPostConstructEvent(this, bean.getName(), instance)), null);

            for (final RuntimeBean child : bean.getChildren()) {
                if (children.stream().noneMatch(b -> b.bean.getName().equals(child.getName()))) {
                    children.add(new BeanContext(this, configuration, child));
                }
            }
        } catch (Throwable x) {
            configuration.fireEvent(l -> l.onFail(new ContextFailEvent(this, bean.getName(), x)), x::addSuppressed);

            try {
                close();
            } catch (Throwable t) {
                x.addSuppressed(t);
            }

            throw x;
        }
    }

    public BeanContext(BeanConfiguration configuration, RuntimeBean root) {
        this(null, configuration, root);
    }

    public Deque<BeanContext> getChildren() {
        return children;
    }

    @Override
    public String getName() {
        return bean.getName();
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
        return getContext(name).instance;
    }

    public BeanContext getContext(String name) {
        if (parent == null) {
            throw new MaridBeanNotFoundException(name);
        } else {
            for (final RuntimeBean brother : parent.bean.getChildren()) {
                if (brother.getName().equals(name)) {
                    try {
                        return parent.children.stream()
                                .filter(c -> c.bean == brother)
                                .findFirst()
                                .orElseGet(() -> {
                                    final BeanContext c = new BeanContext(parent, configuration, brother);
                                    parent.children.add(c);
                                    return c;
                                });
                    } catch (CircularBeanException x) {
                        // continue
                    }
                }
            }
            return parent.getContext(name);
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
    public RuntimeBean getBean() {
        return bean;
    }

    private Object create(RuntimeBean bean, BeanContext context) {
        if (processing.add(bean)) {
            try {
                return bean.getFactory().evaluate(null, null, context);
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
        try {
            for (final Iterator<BeanContext> i = children.descendingIterator(); i.hasNext(); ) {
                final BeanContext child = i.next();
                try {
                    child.close();
                } catch (Throwable x) {
                    e.addSuppressed(x);
                }
            }
            final BeanPreDestroyEvent event = new BeanPreDestroyEvent(this, bean.getName(), instance, e::addSuppressed);
            configuration.fireEvent(l -> l.onPreDestroy(event), e::addSuppressed);
        } catch (Throwable x) {
            e.addSuppressed(x);
        } finally {
            if (parent != null) {
                parent.children.remove(this);
            }
        }
        if (e.getSuppressed().length > 0) {
            throw e;
        }
    }

    private Stream<BeanContext> contexts() {
        return parent == null ? of(this) : concat(parent.contexts(), of(this));
    }

    private Stream<BeanContext> children() {
        return children.stream().flatMap(e -> concat(of(e), e.children()));
    }

    public Object findBean(@Nonnull String name) {
        return children()
                .filter(e -> e.bean.getName().equals(name))
                .findFirst()
                .map(BeanContext::getInstance)
                .orElseThrow(() -> new MaridBeanNotFoundException(name));
    }

    @Override
    public String toString() {
        return contexts().map(c -> c.bean.getName()).collect(joining("/"));
    }

    private static final class CircularBeanException extends RuntimeException {

        private CircularBeanException() {
            super(null, null, false, false);
        }
    }
}
