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

package org.marid.jfx.list;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.Collections;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
public class EmptyListChange<E> extends ListChangeListener.Change<E> {

    public EmptyListChange(ObservableList<E> list) {
        super(list);
    }

    @Override
    public boolean next() {
        return false;
    }

    @Override
    public void reset() {
    }

    @Override
    public int getFrom() {
        return 0;
    }

    @Override
    public int getTo() {
        return 0;
    }

    @Override
    public List<E> getRemoved() {
        return Collections.emptyList();
    }

    @Override
    protected int[] getPermutation() {
        return new int[0];
    }
}
