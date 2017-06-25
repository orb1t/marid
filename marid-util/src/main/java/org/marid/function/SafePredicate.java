package org.marid.function;

import java.util.function.Predicate;

/**
 * @author Dmitry Ovchinnikov
 */
@FunctionalInterface
public interface SafePredicate<T> extends Predicate<T> {

    boolean testUnsafe(T arg) throws Exception;

    default boolean test(T arg) {
        try {
            return testUnsafe(arg);
        } catch (Exception x) {
            throw new IllegalStateException(x);
        }
    }
}
