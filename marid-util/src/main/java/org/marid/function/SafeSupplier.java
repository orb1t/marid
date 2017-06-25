package org.marid.function;

import java.util.function.Supplier;

/**
 * @author Dmitry Ovchinnikov
 */
@FunctionalInterface
public interface SafeSupplier<T> extends Supplier<T> {

    T getUnsafe() throws Exception;

    @Override
    default T get() {
        try {
            return getUnsafe();
        } catch (Exception x) {
            throw new IllegalStateException(x);
        }
    }
}
