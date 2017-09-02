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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.function.Consumer;

import static java.lang.String.format;
import static java.util.logging.Level.WARNING;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
public final class MaridContext implements AutoCloseable {

    final LinkedHashMap<String, Object> beans;

    private final String name;
    private final MaridConfiguration configuration;
    private final MaridContext parent;
    private final List<MaridContext> children;

    private MaridContext(MaridConfiguration configuration,
                         MaridContext parent,
                         MaridCreationContext pcc,
                         Bean bean) {
        this.name = bean.name;
        this.beans = new LinkedHashMap<>(bean.children.size());
        this.parent = parent;
        this.configuration = configuration;
        this.children = new ArrayList<>(bean.children.size());

        try (final MaridCreationContext cc = new MaridCreationContext(pcc, bean, this, configuration.placeholderResolver)) {
            configuration.fireEvent(false, l -> l.bootstrap(new ContextBootstrapEvent(this, cc.runtime)));
            for (final Bean b : bean.children) {
                try {
                    children.add(new MaridContext(configuration, this, pcc, b));
                    cc.getOrCreate(b.name);
                } catch (Throwable x) {
                    configuration.fireEvent(false, l -> l.onFail(new ContextFailEvent(this, b.name, x)));
                    throw x;
                }
            }
            configuration.fireEvent(false, l -> l.onStart(new ContextStartEvent(this)));
        } catch (Throwable x) {
            close();
            throw x;
        }
    }

    public MaridContext(Bean root, ClassLoader classLoader, Properties systemProperties) {
        this(new MaridConfiguration(classLoader, systemProperties), null, null, root);
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

    public Object getBean(String name) {
        return beans.get(name);
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
        final ArrayList<Entry<String, Object>> entries = new ArrayList<>(beans.entrySet());
        beans.clear();
        for (int i = entries.size() - 1; i >= 0; i--) {
            final Entry<String, Object> entry = entries.get(i);
            final String name = entry.getKey();
            final Object instance = entry.getValue();
            destroy(name, instance, x -> e.addSuppressed(new IllegalStateException(format("[%s] Close", name), x)));
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
