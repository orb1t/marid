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

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import org.marid.ide.project.ProjectManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

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
    private void initNameColumn() {
        final TreeTableColumn<Path, Path> column = new TreeTableColumn<>();
        column.textProperty().bind(ls("Name"));
        column.setCellValueFactory(e -> e.getValue().valueProperty());
        column.setCellFactory(e -> new TreeTableCell<Path, Path>() {
            @Override
            protected void updateItem(Path item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.getFileName().toString());
                }
            }
        });
        column.setMinWidth(300);
        column.setPrefWidth(600);
        column.setMaxWidth(2000);
        getColumns().add(column);
    }
}
