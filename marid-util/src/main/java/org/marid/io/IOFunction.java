package org.marid.io;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Function;

/**
 * @author Dmitry Ovchinnikov
 */
@FunctionalInterface
public interface IOFunction<T, R> extends Function<T, R> {

    R ioApply(T arg1) throws IOException;

    @Override
    default R apply(T t) throws UncheckedIOException {
        try {
            return ioApply(t);
        } catch (IOException x) {
            throw new UncheckedIOException(x);
        }
    }
}
