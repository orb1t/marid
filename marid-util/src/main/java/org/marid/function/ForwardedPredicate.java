package org.marid.function;

import java.util.function.Predicate;

/**
 * @author Dmitry Ovchinnikov
 */
public class ForwardedPredicate<T> implements Predicate<T> {

    private final Predicate<T> delegate;

    public ForwardedPredicate(Predicate<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean test(T t) {
        return delegate.test(t);
    }
}
