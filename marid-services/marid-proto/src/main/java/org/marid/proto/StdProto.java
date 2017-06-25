package org.marid.proto;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class StdProto implements Proto {

    private final String id;
    private final String name;

    public StdProto(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return Proto.label(this);
    }
}
