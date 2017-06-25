package org.marid.function;

import java.util.function.Function;

/**
 * @author Dmitry Ovchinnikov
 */
@FunctionalInterface
public interface SafeFunction<T, R> extends Function<T, R> {

    @Override
    default R apply(T t) {
        try {
            return applyUnsafe(t);
        } catch (RuntimeException x) {
            throw x;
        } catch (Exception x) {
            throw new IllegalStateException(x);
        }
    }

    R applyUnsafe(T arg) throws Exception;
}
