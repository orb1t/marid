/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.marid.ide.panes.structure;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeItem.TreeModificationEvent;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import org.marid.ide.common.Directories;
import org.marid.ide.event.FileAddedEvent;
import org.marid.ide.event.FileChangedEvent;
import org.marid.ide.event.FileMovedEvent;
import org.marid.ide.event.FileRemovedEvent;
import org.marid.ide.project.ProjectManager;
import org.marid.ide.structure.editor.FileEditor;
import org.marid.ide.structure.icons.FileIcons;
import org.marid.jfx.LocalizedStrings;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.action.SpecialActions;
import org.marid.jfx.table.MaridTreeTableView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.logging.Level.WARNING;
import static org.marid.jfx.LocalizedStrings.ls;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class ProjectStructureTree extends MaridTreeTableView<Path> {

    @Autowired
    public ProjectStructureTree(Directories directories, SpecialActions specialActions) {
        super(new TreeItem<>(directories.getProfiles()), specialActions);
        getRoot().setExpanded(true);
        getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    @Autowired
    @Order(1)
    private void initNameColumn(FileIcons icons) {
        final TreeTableColumn<Path, Path> column = new TreeTableColumn<>();
        column.textProperty().bind(ls("Name"));
        column.setCellValueFactory(e -> e.getValue().valueProperty());
        column.setCellFactory(e -> new TreeTableCell<Path, Path>() {
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
    }

    @Autowired
    @Order(2)
    private void initSizeColumn() {
        final TreeTableColumn<Path, String> column = new TreeTableColumn<>();
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
    }

    @Autowired
    private void init(Map<String, FileEditor> fileEditors) {
        fileEditors.forEach((name, editor) -> {
            actions.add((item -> {
                if (item == null || item.getValue() == null) {
                    return null;
                } else {
                    final Path file = item.getValue();
                    final Runnable task = editor.getEditAction(file);
                    if (task != null) {
                        return new FxAction(name, editor.getGroup(), "Actions")
                                .bindText(LocalizedStrings.ls(editor.getName()))
                                .bindIcon(new SimpleStringProperty(editor.getIcon()))
                                .setDisabled(false)
                                .setSpecialAction(editor.getSpecialAction())
                                .setEventHandler(e -> task.run());
                    } else {
                        return null;
                    }
                }
            }));

        });
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
