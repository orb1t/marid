package org.marid.function;

/**
 * @author Dmitry Ovchinnikov
 */
@FunctionalInterface
public interface TriConsumer<A1, A2, A3> {

    void accept(A1 arg1, A2 arg2, A3 arg3);
}
