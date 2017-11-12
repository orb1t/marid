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

package org.marid.jfx.list;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ListView;
import org.marid.jfx.action.FxAction;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.OptionalInt;

import static org.marid.jfx.LocalizedStrings.ls;

/**
 * @author Dmitry Ovchinnikov.
 */
public interface MaridListActions {

  static FxAction removeAction(ListView<?> listView) {
    return new FxAction("itemActions", "itemActions", "Actions")
        .bindText(ls("Remove"))
        .setEventHandler(event -> listView.getItems().removeAll(listView.getSelectionModel().getSelectedItems()))
        .bindDisabled(Bindings.isEmpty(listView.getSelectionModel().getSelectedItems()))
        .setIcon("M_REMOVE");
  }

  static FxAction clearAction(ListView<?> listView) {
    return new FxAction("itemsActions", "itemsActions", "Actions")
        .bindText(ls("Clear"))
        .setEventHandler(event -> listView.getItems().clear())
        .bindDisabled(Bindings.isEmpty(listView.getItems()))
        .setIcon("M_CLEAR_ALL");
  }

  static FxAction addAction(String text, EventHandler<ActionEvent> eventHandler) {
    return new FxAction("itemActions", "itemActions", "Actions")
        .bindText(ls(text))
        .setEventHandler(eventHandler)
        .setIcon("M_ADD");
  }

  static <T> FxAction upAction(ListView<T> listView) {
    return new FxAction("upDownActions", "upDownActions", "Actions")
        .bindText(ls("Up"))
        .setIcon("D_MENU_UP")
        .setEventHandler(event -> {
          final int[] indices = listView.getSelectionModel().getSelectedIndices()
              .stream()
              .mapToInt(Integer::intValue)
              .sorted()
              .toArray();
          listView.getSelectionModel().clearSelection();
          for (final int index : indices) {
            final T old = listView.getItems().remove(index);
            listView.getItems().add(index - 1, old);
            listView.getSelectionModel().select(index - 1);
          }
        })
        .bindDisabled(Bindings.createBooleanBinding(() -> {
          final OptionalInt min = listView.getSelectionModel().getSelectedIndices().stream()
              .mapToInt(Integer::intValue)
              .min();
          return (!min.isPresent() || min.getAsInt() <= 0);
        }, listView.getSelectionModel().getSelectedIndices()));
  }

  static <T> FxAction downAction(ListView<T> listView) {
    return new FxAction("upDownActions", "upDownActions", "Actions")
        .bindText(ls("Down"))
        .setIcon("D_MENU_DOWN")
        .setEventHandler(event -> {
          final int[] indices = listView.getSelectionModel().getSelectedIndices()
              .stream()
              .sorted(Comparator.reverseOrder())
              .mapToInt(Integer::intValue)
              .toArray();
          listView.getSelectionModel().clearSelection();
          for (final int index : indices) {
            final T old = listView.getItems().remove(index);
            listView.getItems().add(index + 1, old);
            listView.getSelectionModel().select(index + 1);
          }
        })
        .bindDisabled(Bindings.createBooleanBinding(() -> {
          final OptionalInt max = listView.getSelectionModel().getSelectedIndices().stream()
              .mapToInt(Integer::intValue)
              .max();
          return !max.isPresent() || max.getAsInt() >= listView.getItems().size() - 1;
        }, listView.getSelectionModel().getSelectedIndices()));
  }

  static <E> Runnable autoScroll(@Nonnull ListView<E> listView) {
    final ListChangeListener<E> listener = c -> {
      while (c.next()) {
        if (c.wasAdded()) {
          final Runnable update = () -> {
            final int size = c.getList().size();
            final int selected = listView.getSelectionModel().getSelectedIndex();
            if (c.getAddedSize() + selected + 1 == size || selected < 0) {
              if (listView.isVisible()) {
                listView.scrollTo(size - 1);
              }
            }
          };
          if (Platform.isFxApplicationThread()) {
            update.run();
          } else {
            Platform.runLater(update);
          }
        }
      }
    };
    listView.getItems().addListener(listener);
    return () -> listView.getItems().removeListener(listener);
  }
}
