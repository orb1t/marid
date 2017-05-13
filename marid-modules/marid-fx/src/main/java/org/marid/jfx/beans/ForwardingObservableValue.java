/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
 * Marid, the free data acquisition and visualization software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.marid.jfx.beans;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
public class ForwardingObservableValue<T, V extends ObservableValue<T>>
        implements ObservableValue<T>, FxObservable {

    protected final V delegate;

    private final Collection<InvalidationListener> invalidationListeners = new ConcurrentLinkedQueue<>();
    private final Collection<ChangeListener<? super T>> changeListeners = new ConcurrentLinkedQueue<>();

    public ForwardingObservableValue(@Nonnull V delegate) {
        this.delegate = delegate;
        delegate.addListener(this::onUpdate);
        delegate.addListener(this::onChange);
    }

    private void clean() {
        FxCleaner.clean(invalidationListeners);
        FxCleaner.clean(changeListeners);
    }

    private void onUpdate(Observable observable) {
        clean();
        invalidationListeners.forEach(l -> l.invalidated(observable));
    }

    private void onChange(ObservableValue<? extends T> observable, T oldValue, T newValue) {
        clean();
        changeListeners.forEach(l -> l.changed(observable, oldValue, newValue));
    }

    @Override
    public void addListener(ChangeListener<? super T> listener) {
        clean();
        changeListeners.add(listener);
    }

    @Override
    public void removeListener(ChangeListener<? super T> listener) {
        changeListeners.remove(listener);
        clean();
    }

    @Override
    public T getValue() {
        clean();
        return delegate.getValue();
    }

    @Override
    public void addListener(InvalidationListener listener) {
        clean();
        invalidationListeners.add(listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        invalidationListeners.remove(listener);
        clean();
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && (
                obj == this || obj instanceof ForwardingObservableValue
                        && ((ForwardingObservableValue) obj).delegate.equals(delegate));
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    @Override
    public Stream<?> listeners() {
        return Stream.concat(invalidationListeners.stream(), changeListeners.stream());
    }
}
