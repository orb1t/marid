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

package org.marid.jfx.track;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.ScrollToEvent;
import javafx.scene.control.SelectionModel;

import java.lang.ref.Cleaner;

/**
 * @author Dmitry Ovchinnikov
 */
public interface Tracks {

  Cleaner CLEANER = Cleaner.create();

  static <T> void track(Control control, ObservableList<T> list, SelectionModel<T> selectionModel) {
    control.getProperties().put("TRACK_SELECTION", true);
    if (!list.isEmpty()) {
      selectionModel.clearAndSelect(list.size() - 1);
    }
    selectionModel.selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue.intValue() == list.size() - 1) {
        control.getProperties().replace("TRACK_SELECTION", false, true);
      } else {
        control.getProperties().replace("TRACK_SELECTION", true, false);
      }
    });
    list.addListener((ListChangeListener.Change<? extends T> c) -> {
      while (c.next()) {
        if (Boolean.TRUE.equals(control.getProperties().get("TRACK_SELECTION"))) {
          if (!list.isEmpty()) {
            final int row = list.size() - 1;
            control.fireEvent(new ScrollToEvent<>(control, control, ScrollToEvent.scrollToTopIndex(), row));
            selectionModel.clearAndSelect(row);
          }
        }
      }
    });
  }
}
