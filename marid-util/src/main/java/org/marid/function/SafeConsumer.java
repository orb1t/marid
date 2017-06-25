package org.marid.function;

import java.util.function.Consumer;

/**
 * @author Dmitry Ovchinnikov
 */
@FunctionalInterface
public interface SafeConsumer<T> extends Consumer<T> {

    void acceptUnsafe(T arg) throws Exception;

    @Override
    default void accept(T arg) {
        try {
            acceptUnsafe(arg);
        } catch (Exception x) {
            throw new IllegalStateException(x);
        }
    }
}
