/*
 * Copyright (c) 2017 Dmitry Ovchinnikov
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

package org.marid.ide.panes.structure;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.TreeItem.TreeModificationEvent;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import org.marid.ide.event.FileAddedEvent;
import org.marid.ide.event.FileChangeEvent;
import org.marid.ide.event.FileRemovedEvent;
import org.marid.ide.project.ProjectManager;
import org.marid.ide.structure.editor.FileEditor;
import org.marid.ide.structure.icons.FileIcons;
import org.marid.jfx.beans.ConstantValue;
import org.marid.jfx.menu.MaridContextMenu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Stream;

import static java.util.logging.Level.WARNING;
import static org.marid.jfx.LocalizedStrings.ls;
import static org.marid.l10n.L10n.s;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class ProjectStructureTree extends TreeTableView<Path> {

    @Autowired
    public ProjectStructureTree(ProjectManager projectManager) {
        super(new TreeItem<>(projectManager.getProfilesDir()));
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        getRoot().setExpanded(true);
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
                return ConstantValue.value(NumberFormat.getIntegerInstance().format(size));
            } catch (Exception x) {
                return ConstantValue.value("-1");
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
    private void initRowFactory(FileEditor... fileEditors) {
        setRowFactory(param -> {
            final TreeTableRow<Path> row = new TreeTableRow<>();
            row.setContextMenu(new MaridContextMenu(m -> {
                m.getItems().clear();

                final Path file = row.getItem();
                if (file == null) {
                    return;
                }

                final Map<String, List<MenuItem>> map = new TreeMap<>();
                for (final FileEditor editor : fileEditors) {
                    final Runnable task = editor.getEditAction(file);
                    if (task != null) {
                        final MenuItem item = new MenuItem(s(editor.getName()), editor.getIcon());
                        item.setOnAction(event -> task.run());
                        map.computeIfAbsent(editor.getGroup(), k -> new ArrayList<>()).add(item);
                    }
                }

                map.values().forEach(items -> {
                    m.getItems().addAll(items);
                    m.getItems().add(new SeparatorMenuItem());
                });
            }));
            return row;
        });
    }

    @EventListener
    private void onStart(ContextStartedEvent event) {
        getSelectionModel().select(getRoot());
    }

    @EventListener
    private void onPathAdd(FileAddedEvent event) {
        Platform.runLater(() -> onAdd(event.getSource(), getRoot()));
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
    private void onPathRemove(FileRemovedEvent event) {
        Platform.runLater(() -> onDelete(event.getSource(), getRoot()));
    }

    private void onDelete(Path path, TreeItem<Path> item) {
        if (!item.getChildren().removeIf(i -> i.getValue().equals(path))) {
            item.getChildren().forEach(i -> onDelete(path, i));
        }
    }

    @EventListener
    private void onPathChange(FileChangeEvent event) {
        Platform.runLater(() -> onChange(event.getSource(), getRoot()));
    }

    private void onChange(Path path, TreeItem<Path> item) {
        if (path.equals(item.getValue())) {
            Event.fireEvent(item, new TreeModificationEvent<>(TreeItem.valueChangedEvent(), item));
        } else {
            item.getChildren().forEach(e -> onChange(path, e));
        }
    }
}
