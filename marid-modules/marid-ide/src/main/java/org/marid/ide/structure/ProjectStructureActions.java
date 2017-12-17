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

package org.marid.ide.structure;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.TreeItem;
import org.marid.ide.structure.editor.FileEditor;
import org.marid.jfx.action.FxAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Map;

import static org.marid.jfx.LocalizedStrings.ls;

@Component
public class ProjectStructureActions {

  private final Map<String, FileEditor> fileEditors;

  @Autowired
  public ProjectStructureActions(Map<String, FileEditor> fileEditors) {
    this.fileEditors = fileEditors;
  }

  public ObservableList<FxAction> actions(SelectionModel<TreeItem<Path>> selectionModel) {
    final TreeItem<Path> item = selectionModel.getSelectedItem();
    if (item == null || item.getValue() == null) {
      return FXCollections.emptyObservableList();
    } else {
      final ObservableList<FxAction> actions = FXCollections.observableArrayList();
      fileEditors.forEach((name, editor) -> {
        final Path file = item.getValue();
        final Runnable task = editor.getEditAction(file);
        if (task != null) {
          final ObservableValue<ObservableList<FxAction>> children = editor.getChildren(file);
          final FxAction action = new FxAction(editor.getSpecialAction());
          actions.add(action
              .bindText(ls(editor.getName()))
              .bindIcon(new SimpleStringProperty(editor.getIcon()))
              .setDisabled(false));
          if (children != null) {
            action.bindChildren(children);
          } else {
            action.setEventHandler(event -> task.run());
          }
        }
      });
      return actions;
    }
  }

  public ObservableValue<ObservableList<FxAction>> actionsValue(SelectionModel<TreeItem<Path>> selectionModel) {
    return Bindings.createObjectBinding(() -> actions(selectionModel), selectionModel.selectedItemProperty());
  }
}
