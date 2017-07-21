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

import org.marid.misc.Initializable;
import org.marid.runtime.beans.Bean;
import org.marid.runtime.beans.BeanEvent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.ServiceLoader.load;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import static java.util.stream.StreamSupport.stream;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
public final class MaridContext implements AutoCloseable {

    final LinkedHashMap<String, Object> beans;
    final ClassLoader classLoader;

    public MaridContext(@Nonnull MaridConfiguration configuration, @Nonnull ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.beans = new LinkedHashMap<>(configuration.beans.length);

        try (final MaridBeanCreationContext cc = new MaridBeanCreationContext(configuration, this)) {
            for (final Bean bean : configuration.beans) {
                try {
                    cc.getOrCreate(bean.name);
                } catch (Throwable x) {
                    fireEvent(MaridContextListener::onFail);
                    return;
                }
            }
            fireEvent(MaridContextListener::onStart);
        }
    }

    public MaridContext(@Nonnull MaridConfiguration configuration) {
        this(configuration, Thread.currentThread().getContextClassLoader());
    }

    void initialize(String name, Object bean) {
        fireEvent(l -> l.onEvent(new BeanEvent(bean, name, "PRE_INIT")));
        if (bean instanceof Initializable) {
            ((Initializable) bean).init();
        }
        fireEvent(l -> l.onEvent(new BeanEvent(bean, name, "POST_INIT")));
    }

    private void destroy(String name, Object bean, Consumer<Throwable> errorConsumer) {
        fireEvent(l -> l.onEvent(new BeanEvent(bean, name, "PRE_DESTROY")));
        if (bean instanceof AutoCloseable) {
            try {
                ((AutoCloseable) bean).close();
            } catch (Throwable x) {
                errorConsumer.accept(x);
            }
        }
        fireEvent(l -> l.onEvent(new BeanEvent(bean, name, "POST_DESTROY")));
    }

    public boolean isActive() {
        return !beans.isEmpty();
    }

    private void fireEvent(Consumer<MaridContextListener> event) {
        final MaridContextListener[] listeners;
        try {
            final ServiceLoader<MaridContextListener> serviceLoader = load(MaridContextListener.class, classLoader);
            final Stream<MaridContextListener> listenerStream = stream(serviceLoader.spliterator(), false);
            listeners = listenerStream.sorted().toArray(MaridContextListener[]::new);
        } catch (Throwable x) {
            log(SEVERE, "Unable to load context listeners", x);
            return;
        }
        for (final MaridContextListener listener : listeners) {
            try {
                event.accept(listener);
            } catch (Throwable x) {
                log(WARNING, "Error in {0}", x, listener);
            }
        }
    }

    @Override
    public void close() {
        fireEvent(MaridContextListener::onStop);
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
            throw e;
        }
    }

}
