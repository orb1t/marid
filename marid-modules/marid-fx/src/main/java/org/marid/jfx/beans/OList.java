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

package org.marid.jfx.beans;

import javafx.collections.ModifiableObservableListBase;
import org.marid.jfx.list.EmptyListChange;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Dmitry Ovchinnikov
 */
public class OList<E> extends ModifiableObservableListBase<E> implements OCleanable {

    protected final List<E> list = new ArrayList<>();

    public OList() {
        OCleaner.register(this);
    }

    @Override
    public E get(int index) {
        return list.get(index);
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    protected void doAdd(int index, E element) {
        list.add(index, element);
    }

    @Override
    protected E doSet(int index, E element) {
        return list.set(index, element);
    }

    @Override
    protected E doRemove(int index) {
        return list.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }

    @Override
    public boolean contains(Object o) {
        return list.contains(o);
    }

    @Override
    public boolean containsAll(@Nonnull Collection<?> c) {
        return list.containsAll(c);
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        list.forEach(action);
    }

    @Override
    public void clear() {
        if (hasListeners()) {
            beginChange();
            nextRemove(0, this);
        }
        list.clear();
        ++modCount;
        if (hasListeners()) {
            endChange();
        }
    }

    public void update(int from, int to) {
        beginChange();
        for (int i = from; i < to; i++) {
            nextUpdate(i);
        }
        endChange();
    }

    public void update(int pos) {
        beginChange();
        nextUpdate(pos);
        endChange();
    }

    @Override
    public void clean() {
        fireChange(new EmptyListChange<>(this));
    }
}
