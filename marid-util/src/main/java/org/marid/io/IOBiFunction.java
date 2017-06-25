package org.marid.io;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.BiFunction;

/**
 * @author Dmitry Ovchinnikov
 */
@FunctionalInterface
public interface IOBiFunction<T, U, R> extends BiFunction<T, U, R> {

    R ioApply(T arg1, U arg2) throws IOException;

    @Override
    default R apply(T t, U u) throws UncheckedIOException {
        try {
            return ioApply(t, u);
        } catch (IOException x) {
            throw new UncheckedIOException(x);
        }
    }
}
