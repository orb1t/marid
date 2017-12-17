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

package org.marid.jfx.utils;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.ObservableList;
import javafx.scene.control.MultipleSelectionModel;

import static javafx.beans.binding.Bindings.createBooleanBinding;

public interface ListUtils {

  static <E> void up(ObservableList<E> list, MultipleSelectionModel<E> selectionModel) {
    final int[] indices = upIndices(selectionModel);
    if (indices.length > 0) {
      selectionModel.clearSelection();
      for (final int index : indices) {
        list.add(index - 1, list.remove(index));
      }
      for (final int index : indices) {
        selectionModel.select(index - 1);
      }
    }
  }

  static int[] upIndices(MultipleSelectionModel<?> selectionModel) {
    final int[] selected = selectionModel.getSelectedIndices().stream()
        .mapToInt(Integer::intValue)
        .sorted()
        .toArray();
    return selected.length > 0 && selected[0] > 0 ? selected : new int[0];
  }

  static <E> BooleanBinding upDisabled(ObservableList<E> list, MultipleSelectionModel<E> selectionModel) {
    return createBooleanBinding(() -> upIndices(selectionModel).length == 0, selectionModel.getSelectedIndices());
  }

  static <E> void down(ObservableList<E> list, MultipleSelectionModel<E> selectionModel) {
    final int[] indices = downIndices(list, selectionModel);
    if (indices.length > 0) {
      selectionModel.clearSelection();
      for (final int index : indices) {
        list.add(index + 1, list.get(index));
        list.remove(index);
      }
      for (final int index : indices) {
        selectionModel.select(index + 1);
      }
    }
  }

  static <E> int[] downIndices(ObservableList<E> list, MultipleSelectionModel<E> selectionModel) {
    final int[] selected = selectionModel.getSelectedIndices().stream()
        .sorted((v1, v2) -> v2 - v1)
        .mapToInt(Integer::intValue)
        .toArray();
    return selected.length > 0 && selected[0] < list.size() - 1 ? selected : new int[0];
  }

  static <E> BooleanBinding downDisabled(ObservableList<E> list, MultipleSelectionModel<E> selectionModel) {
    return createBooleanBinding(() -> downIndices(list, selectionModel).length == 0, selectionModel.getSelectedIndices());
  }

  static <E> void remove(ObservableList<E> list, MultipleSelectionModel<E> selectionModel) {
    final int[] selected = selectionModel.getSelectedIndices().stream()
        .sorted((v1, v2) -> v2 - v1)
        .mapToInt(Integer::intValue)
        .toArray();
    for (final int index : selected) {
      list.remove(index);
    }
  }

  static BooleanBinding removeDisabled(MultipleSelectionModel<?> selectionModel) {
    return Bindings.isEmpty(selectionModel.getSelectedIndices());
  }

  static BooleanBinding clearDisabled(ObservableList<?> list) {
    return Bindings.isEmpty(list);
  }
}
