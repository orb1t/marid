package org.marid.ide.types;

import java.util.Set;

/**
 * @author Dmitry Ovchinnikov
 */
public class ComplexBean<T, E extends Set<T>, U> {

    private final E set;
    public final U arg;

    public ComplexBean(E set, U arg) {
        this.set = set;
        this.arg = arg;
    }

    public E getSet() {
        return set;
    }
}
