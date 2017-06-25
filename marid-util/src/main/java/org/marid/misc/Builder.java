package org.marid.misc;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Dmitry Ovchinnikov.
 */
public class Builder<T> {

    final T builder;

    public Builder(T builder) {
        this.builder = builder;
    }

    public <A> Builder<T> set(BiConsumer<T, A> consumer, A arg) {
        consumer.accept(builder, arg);
        return this;
    }

    public T build() {
        return builder;
    }

    public static <T> T build(T arg, Consumer<T> consumer) {
        consumer.accept(arg);
        return arg;
    }

    public static <T> T getFrom(Supplier<T> supplier, Supplier<T> newObjectSupplier, Consumer<T> consumer) {
        final T oldValue = supplier.get();
        if (oldValue == null) {
            final T newValue = newObjectSupplier.get();
            consumer.accept(newValue);
            return newValue;
        } else {
            return oldValue;
        }
    }
}
