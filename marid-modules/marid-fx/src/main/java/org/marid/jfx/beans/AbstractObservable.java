package org.marid.jfx.beans;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class AbstractObservable implements Observable, OCleanable {

    protected final Collection<InvalidationListener> listeners = new ConcurrentLinkedQueue<>();

    public AbstractObservable() {
        OCleaner.register(this);
    }

    @Override
    public void addListener(InvalidationListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        listeners.remove(listener);
    }

    protected void fireInvalidate(Observable observable) {
        listeners.forEach(l -> l.invalidated(observable));
    }

    @Override
    public void clean() {
        fireInvalidate(this);
    }
}
