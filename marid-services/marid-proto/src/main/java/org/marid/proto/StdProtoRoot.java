package org.marid.proto;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
public class StdProtoRoot extends StdProto implements ProtoRoot {

    private final Map<String, StdProtoBus> children = new LinkedHashMap<>();
    private final ThreadGroup threadGroup;

    public StdProtoRoot(String id, String name) {
        super(id, name);
        this.threadGroup = new ThreadGroup(id);
    }

    @Override
    public Map<String, StdProtoBus> getItems() {
        return children;
    }

    public ThreadGroup getThreadGroup() {
        return threadGroup;
    }
}
