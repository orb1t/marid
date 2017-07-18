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
import org.marid.runtime.beans.BeanListener;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import java.util.function.Consumer;

import static java.lang.String.format;

/**
 * @author Dmitry Ovchinnikov
 */
public final class MaridContext implements AutoCloseable {

    final LinkedHashMap<String, Object> beans;
    private final ServiceLoader<BeanListener> beanListeners;

    public MaridContext(@Nonnull MaridConfiguration context, @Nonnull ClassLoader classLoader) {
        this.beans = new LinkedHashMap<>(context.beans.length);
        this.beanListeners = ServiceLoader.load(BeanListener.class, classLoader);

        try (final MaridBeanCreationContext cc = new MaridBeanCreationContext(context, this, classLoader)) {
            for (final Bean bean : context.beans) {
                cc.getOrCreate(bean.name);
            }
        } finally {
            beanListeners.reload();
        }
    }

    void initialize(String name, Object bean) throws Exception {
        beanListeners.forEach(l -> l.onEvent(new BeanEvent(bean, name, "PRE_INIT")));
        if (bean instanceof Initializable) {
            ((Initializable) bean).init();
        }
        beanListeners.forEach(l -> l.onEvent(new BeanEvent(bean, name, "POST_INIT")));
    }

    private void destroy(String name, Object bean, Consumer<Throwable> errorConsumer) {
        beanListeners.forEach(l -> l.onEvent(new BeanEvent(bean, name, "PRE_DESTROY")));
        if (bean instanceof AutoCloseable) {
            try {
                ((AutoCloseable) bean).close();
            } catch (Throwable x) {
                errorConsumer.accept(x);
            }
        }
        beanListeners.forEach(l -> l.onEvent(new BeanEvent(bean, name, "POST_DESTROY")));
    }

    public boolean isActive() {
        return !beans.isEmpty();
    }

    @Override
    public void close() {
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
