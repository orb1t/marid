package org.marid.function;

/**
 * @author Dmitry Ovchinnikov
 */
@FunctionalInterface
public interface SafeTriConsumer<A1, A2, A3> extends TriConsumer<A1, A2, A3> {

    @Override
    default void accept(A1 arg1, A2 arg2, A3 arg3) {
        try {
            acceptUnsafe(arg1, arg2, arg3);
        } catch (Exception x) {
            throw new IllegalStateException(x);
        }
    }

    void acceptUnsafe(A1 arg1, A2 arg2, A3 arg3) throws Exception;
}
