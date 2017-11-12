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

package org.marid.jfx.control;

import javafx.beans.binding.Bindings;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.action.SpecialActionType;
import org.marid.jfx.action.SpecialActions;
import org.marid.jfx.annotation.DisableStdSelectAndRemoveActions;

import javax.annotation.Resource;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public interface MaridActionsTreeControl<T> extends MaridActionsControl<TreeItem<T>> {

  TreeItem<T> getRoot();

  @Resource
  default void setSelectAndRemoveActions(SpecialActions specialActions) {
    if (getClass().isAnnotationPresent(DisableStdSelectAndRemoveActions.class)) {
      return;
    }

    actions().add(e -> new FxAction(specialActions.get(SpecialActionType.REMOVE))
        .bindDisabled(Bindings.isEmpty(getSelectionModel().getSelectedItems()))
        .setEventHandler(event -> {
          final Map<TreeItem<T>, List<TreeItem<T>>> map = getSelectionModel().getSelectedItems().stream()
              .collect(Collectors.groupingBy(TreeItem::getParent, IdentityHashMap::new, toList()));
          map.forEach((k, v) -> k.getChildren().removeAll(v));
        })
    );

    actions().add(e -> new FxAction(specialActions.get(SpecialActionType.SELECT_ALL))
        .setDisabled(getSelectionModel().getSelectionMode().equals(SelectionMode.SINGLE))
        .setEventHandler(event -> getSelectionModel().selectAll())
    );

    actions().add(e -> new FxAction(specialActions.get(SpecialActionType.CLEAR_ALL))
        .setEventHandler(event -> {
          if (getSelectionModel().getSelectedItems().isEmpty()) {
            getRoot().getChildren().clear();
          } else {
            getSelectionModel().getSelectedItems().forEach(i -> i.getChildren().clear());
          }
        })
    );
  }
}
