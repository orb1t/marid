/*
 * Copyright (C) 2014 Dmitry Ovchinnikov
 * Marid, the free data acquisition and visualization software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.marid.util;

import org.marid.functions.SafeFunction;

/**
 * @author Dmitry Ovchinnikov.
 */
public class MaridClassValue<T> extends ClassValue<T> {

    private final SafeFunction<Class<?>, T> function;

    public MaridClassValue(SafeFunction<Class<?>, T> function) {
        this.function = function;
    }

    @Override
    protected T computeValue(Class<?> type) {
        return function.apply(type);
    }

    public static Class<?>[] getCallContext() {
        return ClassResolver.CLASS_RESOLVER.getClassContext().clone();
    }

    public static Class<?> getCaller(Class<?> after) {
        final Class<?>[] context = ClassResolver.CLASS_RESOLVER.getClassContext();
        for (int i = 0; i < context.length; i++) {
            final Class<?> current = context[i];
            if (current == after && i < context.length - 1) {
                return context[i + 1];
            }
        }
        return null;
    }

    public static Class<?> getCaller(int index) {
        return ClassResolver.CLASS_RESOLVER.getClassContext()[index];
    }

    private static final class ClassResolver extends SecurityManager {

        @Override
        protected Class[] getClassContext() {
            return super.getClassContext();
        }

        private static final ClassResolver CLASS_RESOLVER = new ClassResolver();
    }
}
