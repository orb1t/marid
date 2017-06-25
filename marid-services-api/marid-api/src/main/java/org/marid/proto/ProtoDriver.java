package org.marid.proto;

import java.io.Closeable;
import java.util.Collections;
import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
public interface ProtoDriver extends Proto, Closeable {

    ProtoBus getParent();

    void start();

    boolean isRunning();

    @Override
    default Map<String, ? extends Proto> getItems() {
        return Collections.emptyMap();
    }
}
