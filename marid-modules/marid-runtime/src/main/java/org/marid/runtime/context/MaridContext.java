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
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.ServiceLoader.load;
import static java.util.logging.Level.WARNING;
import static java.util.stream.StreamSupport.stream;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
public final class MaridContext implements AutoCloseable {

    private final MaridConfiguration configuration;
    private final MaridContext parent;
    private final LinkedHashMap<String, Object> beans;
    private final LinkedList<MaridContext> children = new LinkedList<>();
    private final MaridContextListener[] listeners;

    private MaridContext(MaridConfiguration configuration,
                         MaridContext parent,
                         MaridCreationContext parentCreationContext,
                         Bean root,
                         ClassLoader classLoader) {
        this.configuration = configuration;
        this.parent = parent;
        this.beans = new LinkedHashMap<>(root.children.size());
        try {
            final ServiceLoader<MaridContextListener> serviceLoader = load(MaridContextListener.class, classLoader);
            final Stream<MaridContextListener> listenerStream = stream(serviceLoader.spliterator(), false);
            listeners = listenerStream.sorted().toArray(MaridContextListener[]::new);
        } catch (Throwable x) {
            throw new IllegalStateException("Unable to load context listeners", x);
        }

        try (final MaridCreationContext cc = new MaridCreationContext(parentCreationContext, root, this)) {
            fireEvent(false, l -> l.bootstrap(new ContextBootstrapEvent(this, cc.runtime)));
            for (final Bean bean : root.children) {
                try {
                    if (!bean.children.isEmpty()) {
                        children.add(new MaridContext(configuration, this, cc, bean, classLoader));
                    }
                    cc.getOrCreate(bean.name);
                } catch (Throwable x) {
                    fireEvent(false, l -> l.onFail(new ContextFailEvent(this, bean.name, x)));
                    throw x;
                }
            }
            fireEvent(false, l -> l.onStart(new ContextStartEvent(this)));
        } catch (Throwable x) {
            close();
            throw x;
        }
    }

    public MaridContext(Bean root, ClassLoader classLoader, Properties systemProperties) {
        this(new MaridConfiguration(classLoader, systemProperties), null, null, root, classLoader);
    }

    public MaridContext(Bean root) {
        this(root, Thread.currentThread().getContextClassLoader(), System.getProperties());
    }

    public LinkedHashMap<String, Object> getBeans() {
        return beans;
    }

    public MaridPlaceholderResolver getPlaceholderResolver() {
        return configuration.placeholderResolver;
    }

    public MaridContext getParent() {
        return parent;
    }

    void initialize(String name, Object bean) {
        fireEvent(false, l -> l.onEvent(new BeanEvent(this, name, bean, "PRE_INIT")));
        fireEvent(false, l -> l.onPostConstruct(new BeanPostConstructEvent(this, name, bean)));
        fireEvent(false, l -> l.onEvent(new BeanEvent(this, name, bean, "POST_INIT")));
    }

    private void destroy(String name, Object bean, Consumer<Throwable> errorConsumer) {
        fireEvent(true, l -> l.onEvent(new BeanEvent(this, name, bean, "PRE_DESTROY")));
        fireEvent(true, l -> l.onPreDestroy(new BeanPreDestroyEvent(this, name, bean, errorConsumer)));
        fireEvent(true, l -> l.onEvent(new BeanEvent(this, name, bean, "POST_DESTROY")));
    }

    private void fireEvent(boolean reverse, Consumer<MaridContextListener> event) {
        for (int i = listeners.length - 1; i >= 0; i--) {
            final MaridContextListener listener = listeners[reverse ? i : listeners.length - i - 1];
            try {
                event.accept(listener);
            } catch (Throwable x) {
                log(WARNING, "Error in {0}", x, listener);
            }
        }
    }

    @Override
    public void close() {
        children.forEach(MaridContext::close);
        fireEvent(true, l -> l.onStop(new ContextStopEvent(this)));
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
