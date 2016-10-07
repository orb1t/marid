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

package org.marid.jfx.util;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.List;

/**
 * @author Dmitry Ovchinnikov
 */
public interface MaridCollections {

    static <E extends Observable> ObservableList<E> list(List<E> list) {
        return fix(FXCollections.observableList(list, e -> new Observable[] {e}));
    }

    static <E extends Observable> ObservableList<E> list() {
        return fix(FXCollections.observableArrayList(e -> new Observable[] {e}));
    }

    static <E extends Observable> ObservableList<E> fix(ObservableList<E> list) {
        final ListChangeListener<E> changeListener = c -> {
            while (c.next()) {
                if (c.wasUpdated()) {
                    for (int i = c.getFrom(); i < c.getTo(); i++) {
                        final E old = list.get(i);
                        list.set(i, null);
                        list.set(i, old);
                    }
                }
            }
        };
        list.addListener(changeListener);
        return list;
    }
}
