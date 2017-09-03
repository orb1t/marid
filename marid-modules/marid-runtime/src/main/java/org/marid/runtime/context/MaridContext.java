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

import org.marid.runtime.beans.Bean;
import org.marid.runtime.event.*;
import org.marid.runtime.exception.MaridBeanNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.lang.String.format;
import static java.util.logging.Level.WARNING;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
public final class MaridContext implements MaridRuntime, AutoCloseable {

    final List<MaridContext> children;
    final MaridConfiguration configuration;
    final Object value;

    private final String name;
    private final MaridContext parent;

    MaridContext(MaridConfiguration conf, MaridContext p, MaridCreationContext pcc, Bean bean, Function<MaridCreationContext, Object> v) {
        this.name = bean.name;
        this.parent = p;
        this.configuration = conf;
        this.children = new ArrayList<>(bean.children.size());

        try (final MaridCreationContext cc = new MaridCreationContext(pcc, bean, this)) {
            conf.fireEvent(false, l -> l.bootstrap(new ContextBootstrapEvent(this)));
            for (final Bean b : bean.children) {
                if (children.stream().noneMatch(c -> c.name.equals(b.name))) {
                    children.add(new MaridContext(conf, this, cc, b, c -> c.create(b)));
                }
            }
            this.value = v.apply(cc);
            conf.fireEvent(false, l -> l.onStart(new ContextStartEvent(this)));
        } catch (Throwable x) {
            close();
            conf.fireEvent(false, l -> l.onFail(new ContextFailEvent(this, name, x)));
            throw x;
        }
    }

    public MaridContext(Bean root, ClassLoader classLoader, Properties systemProperties) {
        this(new MaridConfiguration(classLoader, systemProperties), null, null, root, c -> null);
    }

    public MaridContext(Bean root) {
        this(root, Thread.currentThread().getContextClassLoader(), System.getProperties());
    }

    public MaridContext getParent() {
        return parent;
    }

    public List<MaridContext> getChildren() {
        return children;
    }

    @Override
    public Object getBean(String name) {
        for (final MaridContext child : children) {
            if (child.name.equals(name)) {
                return child.value;
            }
        }
        if (parent == null) {
            throw new MaridBeanNotFoundException(name);
        } else {
            return parent.getBean(name);
        }
    }

    @Override
    public ClassLoader getClassLoader() {
        return configuration.getClassLoader();
    }

    @Override
    public String resolvePlaceholders(String value) {
        return configuration.placeholderResolver.resolvePlaceholders(value);
    }

    @Override
    public Properties getApplicationProperties() {
        return configuration.placeholderResolver.getProperties();
    }

    void initialize(String name, Object bean) {
        configuration.fireEvent(false, l -> l.onEvent(new BeanEvent(this, name, bean, "PRE_INIT")));
        configuration.fireEvent(false, l -> l.onPostConstruct(new BeanPostConstructEvent(this, name, bean)));
        configuration.fireEvent(false, l -> l.onEvent(new BeanEvent(this, name, bean, "POST_INIT")));
    }

    private void destroy(String name, Object bean, Consumer<Throwable> errorConsumer) {
        configuration.fireEvent(true, l -> l.onEvent(new BeanEvent(this, name, bean, "PRE_DESTROY")));
        configuration.fireEvent(true, l -> l.onPreDestroy(new BeanPreDestroyEvent(this, name, bean, errorConsumer)));
        configuration.fireEvent(true, l -> l.onEvent(new BeanEvent(this, name, bean, "POST_DESTROY")));
    }

    @Override
    public void close() {
        final IllegalStateException e = new IllegalStateException("Runtime close exception");
        for (int i = children.size() - 1; i >= 0; i--) {
            final MaridContext child = children.get(i);
            destroy(child.name, child.value, x -> {
                final IllegalStateException ex = new IllegalStateException(format("[%s] Close", child.name), x);
                e.addSuppressed(ex);
            });
        }
        configuration.fireEvent(true, l -> l.onStop(new ContextStopEvent(this)));
        if (e.getSuppressed().length > 0) {
            log(WARNING, "Error on close {0}", e, this);
        }
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(name);
        for (MaridContext c = parent; c != null; c = c.parent) {
            builder.insert(0, c.name + "/");
        }
        return builder.toString();
    }
}
