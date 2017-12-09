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

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.TreeItem.TreeModificationEvent;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import org.marid.ide.common.Directories;
import org.marid.ide.event.FileAddedEvent;
import org.marid.ide.event.FileChangedEvent;
import org.marid.ide.event.FileMovedEvent;
import org.marid.ide.event.FileRemovedEvent;
import org.marid.ide.project.ProjectManager;
import org.marid.ide.structure.icons.FileIcons;
import org.marid.jfx.action.SpecialActions;
import org.marid.misc.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Stream;

import static java.util.logging.Level.WARNING;
import static org.marid.jfx.LocalizedStrings.ls;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class ProjectStructureTree extends TreeTableView<Path> {

  private final TreeTableColumn<Path, Path> nameColumn;
  private final TreeTableColumn<Path, String> sizeColumn;

  @Autowired
  public ProjectStructureTree(Directories directories, FileIcons icons) {
    super(new TreeItem<>(directories.getProfiles()));
    setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
    getRoot().setExpanded(true);
    getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

    nameColumn = Builder.build(new TreeTableColumn<>(), column -> {
      column.textProperty().bind(ls("Name"));
      column.setCellValueFactory(e -> e.getValue().valueProperty());
      column.setCellFactory(e -> new TreeTableCell<>() {
        @Override
        protected void updateItem(Path item, boolean empty) {
          super.updateItem(item, empty);
          if (empty) {
            setGraphic(null);
            setText(null);
          } else {
            setText(item.getFileName().toString());
            setGraphic(icons.icon(item, 16));
          }
        }
      });
      column.setMinWidth(300);
      column.setPrefWidth(600);
      column.setMaxWidth(2000);
      getColumns().add(column);
    });

    sizeColumn = Builder.build(new TreeTableColumn<>(), column -> {
      column.textProperty().bind(ls("Size"));
      column.setCellValueFactory(e -> {
        final Path path = e.getValue().getValue();
        try {
          final long size;
          if (Files.isDirectory(path)) {
            try (final Stream<Path> stream = Files.walk(path)) {
              size = stream.map(Path::toFile).mapToLong(File::length).sum();
            }
          } else {
            size = path.toFile().length();
          }
          return new SimpleStringProperty(NumberFormat.getIntegerInstance().format(size));
        } catch (Exception x) {
          return new SimpleStringProperty("-1");
        }
      });
      column.setCellFactory(e -> {
        final TreeTableCell<Path, String> cell = new TextFieldTreeTableCell<>();
        cell.setAlignment(Pos.BASELINE_RIGHT);
        return cell;
      });
      column.setPrefWidth(200);
      column.setMaxWidth(400);
      column.setMinWidth(200);
      getColumns().add(column);
    });
  }

  @Autowired
  private void init(ProjectStructureActions actions, SpecialActions specialActions) {
    specialActions.assign(focusedProperty(), actions.actionsValue(getSelectionModel()));
    setRowFactory(param -> specialActions.wrap(new TreeTableRow<>(), () -> actions.actions(getSelectionModel())));
  }

  @Autowired
  private void initProfileSelector(ProjectManager projectManager) {
    getSelectionModel().selectedItemProperty().addListener((o, oV, nV) -> {
      if (nV != null) {
        projectManager.getProfile(nV.getValue())
            .filter(p -> !p.equals(projectManager.getProfile()))
            .ifPresent(p -> projectManager.profileProperty().set(p));
      }
    });
  }

  @EventListener
  private void onPathAdd(FileAddedEvent event) {
    Platform.runLater(() -> doWithSelection(() -> onAdd(event.getSource(), getRoot())));
  }

  private void onAdd(Path path, TreeItem<Path> item) {
    if (path.equals(item.getValue())) {
      Event.fireEvent(item, new TreeModificationEvent<>(TreeItem.valueChangedEvent(), item));
      return;
    }
    if (!path.startsWith(item.getValue())) {
      return;
    }
    if (item.getChildren().stream().map(TreeItem::getValue).anyMatch(path::startsWith)) {
      item.getChildren().forEach(e -> onAdd(path, e));
    } else {
      final TreeItem<Path> newPathItem = new TreeItem<>(path);
      newPathItem.setExpanded(!path.getFileName().toString().equals("target"));
      final Comparator<TreeItem<Path>> comparator = (i1, i2) -> {
        if (Files.isDirectory(i1.getValue()) && Files.isDirectory(i2.getValue())) {
          return i1.getValue().compareTo(i2.getValue());
        } else if (Files.isRegularFile(i1.getValue()) && Files.isRegularFile(i2.getValue())) {
          return i1.getValue().compareTo(i2.getValue());
        } else if (Files.isDirectory(i1.getValue())) {
          return -1;
        } else {
          return 1;
        }
      };
      final int index = Collections.binarySearch(item.getChildren(), newPathItem, comparator);
      if (index >= 0) {
        log(WARNING, "Duplicate detected: {0}", path);
      } else {
        item.getChildren().add(-(index + 1), newPathItem);
      }
    }
  }

  @EventListener
  private void onPathMove(FileMovedEvent event) {
    Platform.runLater(() -> doWithSelection(() -> {
      onDelete(event.getSource(), getRoot());
      onAdd(event.getTarget(), getRoot());
    }));
  }

  @EventListener
  private void onPathRemove(FileRemovedEvent event) {
    Platform.runLater(() -> doWithSelection(() -> onDelete(event.getSource(), getRoot())));
  }

  private void onDelete(Path path, TreeItem<Path> item) {
    if (!item.getChildren().removeIf(i -> i.getValue().equals(path))) {
      item.getChildren().forEach(i -> onDelete(path, i));
    }
  }

  @EventListener
  private void onPathChange(FileChangedEvent event) {
    Platform.runLater(() -> doWithSelection(() -> onChange(event.getSource(), getRoot())));
  }

  private void onChange(Path path, TreeItem<Path> item) {
    if (path.equals(item.getValue())) {
      Event.fireEvent(item, new TreeModificationEvent<>(TreeItem.valueChangedEvent(), item));
    } else {
      item.getChildren().forEach(e -> onChange(path, e));
    }
  }

  private void doWithSelection(Runnable runnable) {
    final TreeItem<Path> selected = getSelectionModel().getSelectedItem();
    getSelectionModel().clearSelection();
    getFocusModel().focus(-1);
    setDisable(true);
    runnable.run();
    setDisable(false);
    if (selected != null) {
      final int row = getRow(selected);
      if (row >= 0) {
        getSelectionModel().focus(row);
        getSelectionModel().select(row);
      }
    }
  }
}
