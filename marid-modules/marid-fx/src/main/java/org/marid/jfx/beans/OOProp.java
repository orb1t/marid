package org.marid.jfx.beans;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;

/**
 * @author Dmitry Ovchinnikov
 */
public class OOProp<T extends Observable> extends OProp<T> {

    private final InvalidationListener listener = o -> fireChanged(false);

    public OOProp(String name) {
        super(name);
    }

    public OOProp(String name, T value) {
        super(name, value);
    }

    @Override
    protected void onChange(T oldValue, T newValue) {
        if (oldValue != null) {
            oldValue.removeListener(listener);
        }
        if (newValue != null) {
            newValue.addListener(listener);
        }
    }
}
