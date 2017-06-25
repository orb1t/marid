package org.marid.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * @author Dmitry Ovchinnikov
 */
@FunctionalInterface
public interface IOCloseable extends Closeable {

    default void closeSafely() throws UncheckedIOException {
        try {
            close();
        } catch (IOException x) {
            throw new UncheckedIOException(x);
        }
    }
}
