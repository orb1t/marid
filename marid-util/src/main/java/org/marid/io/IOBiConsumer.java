package org.marid.io;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.BiConsumer;

/**
 * @author Dmitry Ovchinnikov
 */
@FunctionalInterface
public interface IOBiConsumer<T, U> extends BiConsumer<T, U> {

    void ioAccept(T t, U u) throws IOException;

    @Override
    default void accept(T t, U u) throws UncheckedIOException {
        try {
            ioAccept(t, u);
        } catch (IOException x) {
            throw new UncheckedIOException(x);
        }
    }
}
