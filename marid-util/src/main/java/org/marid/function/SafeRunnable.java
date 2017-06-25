package org.marid.function;

/**
 * @author Dmitry Ovchinnikov
 */
public interface SafeRunnable extends Runnable {

    void runUnsafe() throws Exception;

    @Override
    default void run() {
        try {
            runUnsafe();
        } catch (Exception x) {
            throw new IllegalStateException(x);
        }
    }

    static Runnable runnable(SafeRunnable runnable) {
        return runnable;
    }
}
