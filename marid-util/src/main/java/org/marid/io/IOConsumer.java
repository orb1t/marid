package org.marid.io;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Consumer;

/**
 * @author Dmitry Ovchinnikov
 */
@FunctionalInterface
public interface IOConsumer<T> extends Consumer<T> {

    @Override
    default void accept(T t) throws UncheckedIOException {
        try {
            ioAccept(t);
        } catch (IOException x) {
            throw new UncheckedIOException(x);
        }
    }

    void ioAccept(T t) throws IOException;
}
