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

package org.marid.functions;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author Dmitry Ovchinnikov
 */
public class Functions {

    public static <T> Consumer<T> safeConsumer(SafeConsumer<T> safeConsumer, Consumer<Exception> exceptionConsumer) {
        return arg -> {
            try {
                safeConsumer.acceptUnsafe(arg);
            } catch (Exception x) {
                exceptionConsumer.accept(x);
            }
        };
    }

    public static <T> Consumer<T> safeConsumer(SafeConsumer<T> safeConsumer) {
        return safeConsumer;
    }

    public static <T, R> Function<T, R> safeFunction(SafeFunction<T, R> safeFunction, Function<Exception, R> exceptionFunction) {
        return t -> {
            try {
                return safeFunction.applyUnsafe(t);
            } catch (Exception x) {
                return exceptionFunction.apply(x);
            }
        };
    }

    public static <T, R> Function<T, R> safeFunction(SafeFunction<T, R> safeFunction) {
        return safeFunction;
    }

    public static <T> Predicate<T> safePredicate(SafePredicate<T> safePredicate, Predicate<Exception> exceptionPredicate) {
        return arg -> {
            try {
                return safePredicate.testUnsafe(arg);
            } catch (Exception x) {
                return exceptionPredicate.test(x);
            }
        };
    }

    public static <T> Predicate<T> safePredicate(SafePredicate<T> safePredicate) {
        return safePredicate;
    }

    public static <T> Supplier<T> safeSupplier(SafeSupplier<T> safeSupplier, Function<Exception, T> exceptionFunction) {
        return () -> {
            try {
                return safeSupplier.getUnsafe();
            } catch (Exception x) {
                return exceptionFunction.apply(x);
            }
        };
    }

    public static <T> Supplier<T> safeSupplier(SafeSupplier<T> safeSupplier) {
        return safeSupplier;
    }
}
