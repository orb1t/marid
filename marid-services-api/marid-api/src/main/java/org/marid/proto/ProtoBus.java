package org.marid.proto;

import java.io.Closeable;
import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
public interface ProtoBus extends Proto, Closeable {

    void reset();

    @Override
    ProtoRoot getParent();

    Map<String, ? extends ProtoDriver> getItems();

    ProtoBusTaskRunner<? extends ProtoBus> getTaskRunner();

    ProtoHealth getHealth();
}
