package org.marid.proto;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
public interface ProtoRoot extends Proto, Closeable {

    @Override
    default Proto getParent() {
        return null;
    }

    Map<String, ? extends ProtoBus> getItems();

    @Override
    default void close() throws IOException {
        final IOException exception = Proto.close(getItems());
        if (exception.getSuppressed().length > 0) {
            throw exception;
        }
    }
}
