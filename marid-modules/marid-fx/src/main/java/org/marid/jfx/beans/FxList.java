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

import com.google.common.collect.ForwardingList;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
public class FxList<E> extends ForwardingList<E> implements ObservableList<E>, FxObservable {

    private final ObservableList<E> delegate;
    private final List<InvalidationListener> invalidationListeners = new LinkedList<>();
    private final List<ListChangeListener<? super E>> changeListeners = new LinkedList<>();

    public FxList(ObservableList<E> delegate) {
        this.delegate = delegate;
        delegate.addListener(this::onInvalidate);
        delegate.addListener(this::onChange);
    }

    public FxList(Function<E, Observable[]> extractor) {
        this(FXCollections.observableArrayList(extractor::apply));
    }

    public FxList() {
        this(FXCollections.observableArrayList());
    }

    private void clean() {
        FxCleaner.clean(invalidationListeners);
        FxCleaner.clean(changeListeners);
    }

    private void onInvalidate(Observable observable) {
        clean();
        invalidationListeners.forEach(l -> l.invalidated(observable));
    }

    private void onChange(ListChangeListener.Change<? extends E> change) {
        clean();
        changeListeners.forEach(l -> l.onChanged(change));
    }

    @Override
    protected List<E> delegate() {
        return delegate;
    }

    @Override
    public void addListener(ListChangeListener<? super E> listener) {
        clean();
        changeListeners.add(listener);
    }

    @Override
    public void removeListener(ListChangeListener<? super E> listener) {
        changeListeners.remove(listener);
        clean();
    }

    @Override
    public boolean addAll(E[] elements) {
        clean();
        return delegate.addAll(elements);
    }

    @Override
    public boolean setAll(E[] elements) {
        clean();
        return delegate.setAll(elements);
    }

    @Override
    public boolean setAll(Collection<? extends E> col) {
        clean();
        return delegate.setAll(col);
    }

    @Override
    public boolean removeAll(E[] elements) {
        clean();
        return delegate.removeAll(elements);
    }

    @Override
    public boolean retainAll(E[] elements) {
        clean();
        return delegate.retainAll(elements);
    }

    @Override
    public void remove(int from, int to) {
        clean();
        delegate.remove(from, to);
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
    public Stream<?> listeners() {
        return Stream.concat(invalidationListeners.stream(), changeListeners.stream());
    }
}
