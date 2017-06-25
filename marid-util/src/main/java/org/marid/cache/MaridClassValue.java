package org.marid.cache;

import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * @author Dmitry Ovchinnikov.
 */
public class MaridClassValue<T> extends ClassValue<T> {

    private final Function<Class<?>, Callable<T>> computeFunction;

    public MaridClassValue(Function<Class<?>, Callable<T>> computeFunction) {
        this.computeFunction = computeFunction;
    }

    @Override
    protected T computeValue(Class<?> type) {
        try {
            return computeFunction.apply(type).call();
        } catch (Exception x) {
            throw new IllegalArgumentException(type.getName(), x);
        }
    }
}
