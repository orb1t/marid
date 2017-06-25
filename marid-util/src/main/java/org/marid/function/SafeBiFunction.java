package org.marid.function;

import java.util.function.BiFunction;

/**
 * @author Dmitry Ovchinnikov
 */
@FunctionalInterface
public interface SafeBiFunction<A1, A2, R> extends BiFunction<A1, A2, R> {

    R applyUnsafe(A1 a1, A2 a2) throws Exception;

    default R apply(A1 a1, A2 a2) {
        try {
            return applyUnsafe(a1, a2);
        } catch (Exception x) {
            throw new IllegalStateException(x);
        }
    }
}
