/*
 *
 */

package org.marid.jfx.beans;

/*-
 * #%L
 * marid-fx
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
public class OOList<E extends Observable> extends OList<E> {

    protected final Map<E, InvalidationListener> listeners = new IdentityHashMap<>();

    @Override
    protected void doAdd(int index, E element) {
        super.doAdd(index, element);
        processAdd(element);
    }

    @Override
    protected E doSet(int index, E element) {
        final E old = super.doSet(index, element);
        processRemove(old);
        processAdd(element);
        return old;
    }

    @Override
    protected E doRemove(int index) {
        final E old = super.doRemove(index);
        processRemove(old);
        return old;
    }

    @Override
    public void clear() {
        listeners.entrySet().removeIf(e -> {
            if (e.getKey() != null) {
                e.getKey().removeListener(e.getValue());
            }
            return true;
        });
        super.clear();
    }

    protected void processAdd(E element) {
        if (element != null) {
            listeners.compute(element, (e, old) -> {
                if (old != null) {
                    return old;
                } else {
                    final InvalidationListener listener = o -> fireUpdate(e);
                    e.addListener(listener);
                    return listener;
                }
            });
        }
    }

    protected void processRemove(E element) {
        if (element != null) {
            if (stream().noneMatch(e -> e == element)) {
                listeners.computeIfPresent(element, (e, old) -> {
                    element.removeListener(old);
                    return null;
                });
            }
        }
    }

    protected void fireUpdate(E element) {
        for (int i = 0; i < size(); i++) {
            final E e = get(i);
            if (e == element) {
                fireChange(new SingleUpdate<>(this, i));
            }
        }
    }

    protected static class SingleUpdate<E> extends ListChangeListener.Change<E> {

        private final int index;
        private boolean state;

        public SingleUpdate(ObservableList<E> list, int index) {
            super(list);
            this.index = index;
        }

        @Override
        public boolean next() {
            if (state) {
                return false;
            } else {
                state = true;
                return true;
            }
        }

        @Override
        public void reset() {
            state = false;
        }

        @Override
        public int getFrom() {
            return index;
        }

        @Override
        public int getTo() {
            return index + 1;
        }

        @Override
        public List<E> getRemoved() {
            return Collections.emptyList();
        }

        @Override
        protected int[] getPermutation() {
            return new int[0];
        }

        @Override
        public boolean wasPermutated() {
            return false;
        }

        @Override
        public boolean wasAdded() {
            return false;
        }

        @Override
        public boolean wasRemoved() {
            return false;
        }

        @Override
        public boolean wasUpdated() {
            return true;
        }

        @Override
        public boolean wasReplaced() {
            return false;
        }

        @Override
        public String toString() {
            return String.format("Update(%d)", index);
        }
    }
}
