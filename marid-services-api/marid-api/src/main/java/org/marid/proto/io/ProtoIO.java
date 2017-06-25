package org.marid.proto.io;

import org.marid.io.IOBiConsumer;
import org.marid.io.IOBiFunction;
import org.marid.io.IOCloseable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Dmitry Ovchinnikov
 */
public interface ProtoIO extends IOCloseable {

    InputStream getInputStream();

    OutputStream getOutputStream();

    default void doWith(IOBiConsumer<InputStream, OutputStream> consumer) throws IOException {
        consumer.accept(getInputStream(), getOutputStream());
    }

    default <T> T call(IOBiFunction<InputStream, OutputStream, T> function) throws IOException {
        return function.apply(getInputStream(), getOutputStream());
    }
}
