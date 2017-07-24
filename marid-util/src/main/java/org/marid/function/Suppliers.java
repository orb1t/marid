/*-
 * #%L
 * marid-util
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

package org.marid.function;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Dmitry Ovchinnikov
 */
public interface Suppliers {

    static <T> Supplier<T> memoized(Supplier<T> supplier) {
        final AtomicReference<T> ref = new AtomicReference<>();
        final AtomicBoolean initialized = new AtomicBoolean();
        return () -> {
            if (initialized.compareAndSet(false, true)) {
                final T value = supplier.get();
                ref.set(value);
                return value;
            } else {
                return ref.get();
            }
        };
    }

    static <T, R> Function<T, R> elseFunc(Function<T, R> func) {
        return v -> v == null ? null : func.apply(v);
    }

    static <T, U, R> BiFunction<T, U, R> elseBiFunc(BiFunction<T, U, R> func) {
        return (t, u) -> t == null ? null : func.apply(t, u);
    }

    static <K, V, E extends Exception> V get(Map<K, V> map, K key, Function<K, E> exceptionFunc) throws E {
        final V value = map.get(key);
        if (value == null) {
            throw exceptionFunc.apply(key);
        } else {
            return value;
        }
    }
}
