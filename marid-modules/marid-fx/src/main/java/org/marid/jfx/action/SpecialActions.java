/*-
 * #%L
 * marid-ide
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

package org.marid.jfx.action;

import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.annotation.Resource;
import java.util.EnumMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.marid.jfx.action.SpecialActionType.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class SpecialActions {

  private final EnumMap<SpecialActionType, SpecialAction> actionMap = new EnumMap<>(SpecialActionType.class);

  @Resource
  public void setAddAction(SpecialAction addAction) {
    actionMap.put(ADD, addAction);
  }

  @Resource
  public void setEditAction(SpecialAction editAction) {
    actionMap.put(EDIT, editAction);
  }

  @Resource
  public void setRemoveAction(SpecialAction removeAction) {
    actionMap.put(REMOVE, removeAction);
  }

  @Resource
  public void setClearAllAction(SpecialAction clearAllAction) {
    actionMap.put(CLEAR_ALL, clearAllAction);
  }

  @Resource
  public void setSelectAllAction(SpecialAction selectAllAction) {
    actionMap.put(SELECT_ALL, selectAllAction);
  }

  @Resource
  public void setCutAction(SpecialAction cutAction) {
    actionMap.put(CUT, cutAction);
  }

  @Resource
  public void setCopyAction(SpecialAction copyAction) {
    actionMap.put(COPY, copyAction);
  }

  @Resource
  public void setPasteAction(SpecialAction pasteAction) {
    actionMap.put(PASTE, pasteAction);
  }

  @Resource
  public void setMiscAction(SpecialAction miscAction) {
    actionMap.put(MISC, miscAction);
  }

  @Resource
  public void setRenameAction(SpecialAction renameAction) {
    actionMap.put(RENAME, renameAction);
  }

  @Resource
  public void setUpAction(SpecialAction upAction) {
    actionMap.put(UP, upAction);
  }

  @Resource
  public void setDownAction(SpecialAction downAction) {
    actionMap.put(DOWN, downAction);
  }

  public SpecialAction get(SpecialActionType type) {
    return actionMap.get(type);
  }

  public void reset() {
    actionMap.values().forEach(SpecialAction::reset);
  }

  public void assign(ObservableValue<ObservableList<FxAction>> actions) {
    final InvalidationListener listener = o -> actionMap.forEach((type, specialAction) -> {
      specialAction.reset();

      final List<FxAction> matched = actions.getValue().stream()
          .filter(a -> type == MISC && a.specialAction == null || a.specialAction == specialAction)
          .collect(Collectors.toList());

      switch (matched.size()) {
        case 0:
          break;
        case 1:
          specialAction.copy(matched.get(0));
          break;
        default:
          specialAction.bindChildren(new SimpleObjectProperty<>(FXCollections.observableArrayList(matched)));
          break;
      }
    });
    actions.addListener(listener);
    listener.invalidated(actions);
  }
}
