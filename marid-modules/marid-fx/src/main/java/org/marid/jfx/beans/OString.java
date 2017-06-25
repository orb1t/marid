package org.marid.jfx.beans;

import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.WritableStringValue;

/**
 * @author Dmitry Ovchinnikov
 */
public class OString extends OProp<String> implements ObservableStringValue, WritableStringValue {

    public OString(String name) {
        super(name);
    }

    public OString(String name, String value) {
        super(name, value);
    }
}
