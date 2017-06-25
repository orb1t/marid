package org.marid.io;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Supplier;

/**
 * @author Dmitry Ovchinnikov
 */
@FunctionalInterface
public interface IOSupplier<T> extends Supplier<T> {

    @Override
    default T get() throws UncheckedIOException {
        try {
            return ioGet();
        } catch (IOException x) {
            throw new UncheckedIOException(x);
        }
    }

    T ioGet() throws IOException;
}
