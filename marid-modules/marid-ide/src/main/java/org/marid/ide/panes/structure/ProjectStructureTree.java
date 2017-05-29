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

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import org.marid.ide.project.ProjectManager;
import org.marid.jfx.beans.ConstantValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.stream.Stream;

import static org.marid.jfx.LocalizedStrings.ls;

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

    @PostConstruct
    private void initRowFactory() {
        setRowFactory(param -> {
            final TreeTableRow<Path> row = new TreeTableRow<>();
            return row;
        });
    }
}
