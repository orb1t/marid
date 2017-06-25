package org.marid.proto;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
public interface Proto {

    String getName();

    String getId();

    Proto getParent();

    Map<String, ? extends Proto> getItems();

    static LinkedList<String> path(Proto proto) {
        final LinkedList<String> path = new LinkedList<>();
        for (Proto p = proto; p != null; p = p.getParent()) {
            path.addFirst(p.getId());
        }
        return path;
    }

    static String label(Proto proto) {
        return proto.getName() + ": " + path(proto);
    }

    static IOException close(Map<String, ? extends Closeable> closeableMap) {
        final IOException exception = new IOException();
        for (final Map.Entry<String, ? extends Closeable> e : closeableMap.entrySet()) {
            final String id = e.getKey();
            final Closeable closeable = e.getValue();
            try {
                closeable.close();
            } catch (IOException x) {
                exception.addSuppressed(new UncheckedIOException(id, x));
            } catch (Exception x) {
                exception.addSuppressed(x);
            }
        }
        return exception;
    }
}
