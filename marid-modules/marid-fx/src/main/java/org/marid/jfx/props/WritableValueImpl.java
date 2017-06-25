package org.marid.jfx.props;

import javafx.beans.value.WritableObjectValue;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Dmitry Ovchinnikov
 */
public class WritableValueImpl<T> implements WritableObjectValue<T> {

    private final Consumer<T> consumer;
    private final Supplier<T> supplier;

    public WritableValueImpl(Consumer<T> consumer, Supplier<T> supplier) {
        this.consumer = consumer;
        this.supplier = supplier;
    }

    @Override
    public T getValue() {
        return get();
    }

    @Override
    public void setValue(T value) {
        set(value);
    }

    @Override
    public T get() {
        return supplier.get();
    }

    @Override
    public void set(T value) {
        consumer.accept(value);
    }
}