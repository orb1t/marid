package org.marid.function;

import java.util.function.BiConsumer;

/**
 * @author Dmitry Ovchinnikov
 */
@FunctionalInterface
public interface SafeBiConsumer<A1, A2> extends BiConsumer<A1, A2> {

    void acceptUnsafe(A1 arg1, A2 arg2) throws Exception;

    default void accept(A1 arg1, A2 arg2) {
        try {
            acceptUnsafe(arg1, arg2);
        } catch (Exception x) {
            throw new IllegalStateException(x);
        }
    }
}
