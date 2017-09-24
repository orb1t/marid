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
import org.marid.runtime.exception.MaridBeanNotFoundException;
import org.marid.runtime.model.MaridBean;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;
import java.util.stream.Stream;

import static java.util.logging.Level.SEVERE;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;
import static org.marid.logging.Log.log;

public final class BeanContext implements MaridRuntime, AutoCloseable {

    private final BeanContext parent;
    private final BeanConfiguration configuration;
    private final MaridBean bean;
    private final Object instance;
    private final LinkedList<BeanContext> children = new LinkedList<>();
    private final HashSet<MaridBean> processing = new HashSet<>();

    public BeanContext(@Nullable BeanContext parent, @Nonnull BeanConfiguration configuration, @Nonnull MaridBean bean) {
        this.parent = parent;
        this.configuration = configuration;
        this.bean = bean;

        configuration.fireEvent(false, l -> l.bootstrap(new ContextBootstrapEvent(this)));

        try {
            this.instance = parent == null ? null : parent.create(bean, this);
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

    public BeanContext(BeanConfiguration configuration, MaridBean root) {
        this(null, configuration, root);
    }

    public LinkedList<BeanContext> getChildren() {
        return children;
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

    private Object create(MaridBean bean, BeanContext context) {
        if (processing.add(bean)) {
            try {
                return bean.getFactory().evaluate(context.instance, context);
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
        for (final Iterator<BeanContext> i = children.descendingIterator(); i.hasNext(); ) {
            final BeanContext child = i.next();
            try {
                try {
                    child.close();
                } catch (Throwable x) {
                    e.addSuppressed(x);
                }
            } finally {
                i.remove();
            }
        }
        configuration.fireEvent(true, l -> l.onPreDestroy(new BeanPreDestroyEvent(this, bean.getName(), instance, e::addSuppressed)));
        if (e.getSuppressed().length > 0) {
            log(SEVERE, "Unable to close {0}", e, this);
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
