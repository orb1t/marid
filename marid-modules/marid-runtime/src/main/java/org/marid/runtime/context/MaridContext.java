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

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;

import static java.lang.String.format;
import static java.util.logging.Level.WARNING;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
public final class MaridContext implements AutoCloseable {

    final LinkedHashMap<String, Object> beans;

    private final MaridConfiguration configuration;
    private final MaridContext parent;
    private final ArrayList<MaridContext> children = new ArrayList<>();

    private MaridContext(MaridConfiguration configuration, MaridContext parent, MaridCreationContext pcc, Bean root) {
        this.parent = parent;
        this.configuration = configuration;
        this.beans = new LinkedHashMap<>(root.children.size());

        try (final MaridCreationContext cc = new MaridCreationContext(pcc, root, this, configuration.placeholderResolver)) {
            configuration.fireEvent(false, l -> l.bootstrap(new ContextBootstrapEvent(this, cc.runtime)));
            for (final Bean bean : root.children) {
                try {
                    if (!bean.children.isEmpty()) {
                        children.add(new MaridContext(configuration, this, pcc, bean));
                    }
                    cc.getOrCreate(bean.name);
                } catch (Throwable x) {
                    configuration.fireEvent(false, l -> l.onFail(new ContextFailEvent(this, bean.name, x)));
                    throw x;
                }
            }
            configuration.fireEvent(false, l -> l.onStart(new ContextStartEvent(this)));
            children.trimToSize();
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

    public Map<String, Object> getBeans() {
        return Collections.unmodifiableMap(beans);
    }

    public MaridContext getParent() {
        return parent;
    }

    public List<MaridContext> getChildren() {
        return children;
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
        children.forEach(MaridContext::close);
        configuration.fireEvent(true, l -> l.onStop(new ContextStopEvent(this)));
        final IllegalStateException e = new IllegalStateException("Runtime close exception");
        final List<Entry<String, Object>> entries = new ArrayList<>(beans.entrySet());
        beans.clear();
        for (int i = entries.size() - 1; i >= 0; i--) {
            final Entry<String, Object> entry = entries.get(i);
            final String name = entry.getKey();
            final Object instance = entry.getValue();
            destroy(name, instance, x -> e.addSuppressed(new IllegalStateException(format("[%s] Close", name), x)));
        }
        if (e.getSuppressed().length > 0) {
            log(WARNING, "Error on close {0}", e, this);
        }
    }
}
