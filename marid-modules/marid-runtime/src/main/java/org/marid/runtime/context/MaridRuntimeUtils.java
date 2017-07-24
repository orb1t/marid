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

import org.marid.runtime.exception.MaridBeanClassLoadingException;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author Dmitry Ovchinnikov
 */
public interface MaridRuntimeUtils {

    static Class<?> loadClass(ClassLoader classLoader, String beanName, String className) {
        try {
            return classLoader.loadClass(className);
        } catch (Exception x) {
            throw new MaridBeanClassLoadingException(beanName, className, x);
        }
    }

    static TreeSet<Method> methods(@Nonnull Object bean,
                                   @Nonnull Predicate<Method> filter,
                                   @Nonnull Comparator<Method> methodComparator) {
        final TreeSet<Method> methods = new TreeSet<>(methodComparator);
        final Consumer<Class<?>> consumer = c -> Stream.of(c.getDeclaredMethods())
                .filter(m -> m.getParameterCount() == 0)
                .filter(filter)
                .peek(m -> m.setAccessible(true))
                .forEach(methods::add);
        for (Class<?> c = bean.getClass(); c != null; c = c.getSuperclass()) {
            consumer.accept(c);
        }
        for (final Class<?> c : bean.getClass().getInterfaces()) {
            consumer.accept(c);
        }
        return methods;
    }
}
