package org.marid.jfx.beans;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WritableObjectValue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * @author Dmitry Ovchinnikov
 */
public class OProp<T> extends ObjectPropertyBase<T> implements WritableObjectValue<T>, OCleanable {

    private final String name;
    private final List<InvalidationListener> invalidationListeners = new LinkedList<>();
    private final List<ChangeListener<? super T>> changeListeners = new LinkedList<>();

    private T oldValue;

    public OProp(String name) {
        this.name = name;
        OCleaner.register(this);
    }

    public OProp(String name, T value) {
        this(name);
        setValue(value);
    }

    @Override
    public Object getBean() {
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void addListener(ChangeListener<? super T> listener) {
        changeListeners.add(listener);
    }

    @Override
    public void removeListener(ChangeListener<? super T> listener) {
        changeListeners.remove(listener);
    }

    @Override
    public void addListener(InvalidationListener listener) {
        invalidationListeners.add(listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        invalidationListeners.remove(listener);
    }

    @Override
    protected void fireValueChangedEvent() {
        fireChanged(true);
    }

    protected void fireChanged(boolean checkEquals) {
        final T newValue = getValue();
        if (!checkEquals || !Objects.equals(oldValue, newValue)) {
            final List<InvalidationListener> invalidationListeners = new ArrayList<>(this.invalidationListeners);
            final List<ChangeListener<? super T>> changeListeners = new ArrayList<>(this.changeListeners);
            invalidationListeners.forEach(l -> l.invalidated(this));
            changeListeners.forEach(l -> l.changed(this, oldValue, newValue));
            onChange(oldValue, newValue);
            oldValue = newValue;
        }
    }

    protected void onChange(T oldValue, T newValue) {
    }

    @Override
    public void clean() {
        fireChanged(false);
    }
}
