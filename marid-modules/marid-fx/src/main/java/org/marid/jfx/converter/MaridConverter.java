package org.marid.jfx.converter;

import javafx.util.StringConverter;

import java.util.function.Function;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
public class MaridConverter<T> extends StringConverter<T> {

    private final Function<T, String> directFunc;
    private final Function<String, T> reverseFunc;

    public MaridConverter(Function<T, String> directFunc, Function<String, T> reverseFunc) {
        this.directFunc = directFunc;
        this.reverseFunc = reverseFunc;
    }

    public MaridConverter(Function<T, String> directFunc) {
        this(directFunc, s -> {
            throw new UnsupportedOperationException(s);
        });
    }

    public MaridConverter(String format) {
        this(o -> String.format(format, o));
    }

    @Override
    public String toString(T object) {
        return directFunc.apply(object);
    }

    @Override
    public T fromString(String string) {
        return reverseFunc.apply(string);
    }
}
