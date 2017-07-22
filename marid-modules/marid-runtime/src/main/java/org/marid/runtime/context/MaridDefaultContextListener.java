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

import org.marid.runtime.beans.BeanEvent;
import org.marid.runtime.exception.MaridBeanInitializationException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.function.Consumer;

import static org.marid.runtime.context.MaridRuntimeUtils.methods;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridDefaultContextListener implements MaridContextListener {

    @Override
    public void onEvent(@Nonnull BeanEvent event) {
    }

    @Override
    public void onInitialize(@Nonnull String name, @Nullable Object bean) {
        if (bean == null) {
            return;
        }
        final Comparator<Method> byClass = this::cmp;
        final Comparator<Method> cmp = byClass.thenComparing(Method::getName);
        final TreeSet<Method> methods = methods(bean, m -> m.isAnnotationPresent(PostConstruct.class), cmp);
        final HashSet<String> passed = new HashSet<>();
        for (final Method method : methods) {
            if (passed.add(method.getName())) {
                try {
                    method.invoke(bean);
                } catch (Throwable x) {
                    throw new MaridBeanInitializationException(name, x);
                }
            }
        }
    }

    @Override
    public void onDestroy(@Nonnull String name, @Nullable Object bean, @Nonnull Consumer<Throwable> throwableConsumer) {
        if (bean == null) {
            return;
        }
        final Comparator<Method> byClass = this::cmp;
        final Comparator<Method> cmp = byClass.thenComparing(Method::getName).reversed();
        final TreeSet<Method> methods = methods(bean, m -> m.isAnnotationPresent(PreDestroy.class), cmp);
        final HashSet<String> passed = new HashSet<>();
        for (final Method method : methods) {
            if (passed.add(method.getName())) {
                try {
                    method.invoke(bean);
                } catch (Throwable x) {
                    throwableConsumer.accept(x);
                }
            }
        }
        if (bean instanceof AutoCloseable) {
            try {
                ((AutoCloseable) bean).close();
            } catch (Throwable x) {
                throwableConsumer.accept(x);
            }
        }
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onStop() {
    }

    @Override
    public void onFail() {
    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE - 100;
    }

    private int cmp(Method m1, Method m2) {
        if (m1.getDeclaringClass().equals(m2.getDeclaringClass())) {
            return 0;
        } else if (m1.getDeclaringClass().isAssignableFrom(m2.getDeclaringClass())) {
            return -1;
        } else {
            return 1;
        }
    }
}
