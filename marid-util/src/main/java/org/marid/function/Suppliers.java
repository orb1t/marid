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

import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Dmitry Ovchinnikov
 */
public interface Suppliers {

    static <T> Supplier<T> memoized(Supplier<T> supplier) {
        final AtomicMarkableReference<T> ref = new AtomicMarkableReference<>(null, false);
        return () -> {
            if (ref.isMarked()) {
                return ref.getReference();
            } else {
                synchronized (ref) {
                    if (ref.isMarked()) {
                        return ref.getReference();
                    } else {
                        final T v = supplier.get();
                        ref.set(v, true);
                        return v;
                    }
                }
            }
        };
    }

    static <T, R> Function<T, R> elseFunc(Function<T, R> func) {
        return v -> v == null ? null : func.apply(v);
    }

    static <T, U, R> BiFunction<T, U, R> elseBiFunc(BiFunction<T, U, R> func) {
        return (t, u) -> t == null ? null : func.apply(t, u);
    }
}
