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

package org.marid.dependant.beantree;

import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import org.marid.dependant.beantree.items.AbstractTreeItem;
import org.marid.dependant.beantree.items.FileTreeItem;
import org.marid.ide.common.SpecialActions;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.action.MaridActions;
import org.marid.jfx.menu.MaridContextMenu;
import org.marid.spring.xml.BeanFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import static org.marid.jfx.LocalizedStrings.ls;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanTree extends TreeTableView<Object> {

    @Autowired
    public BeanTree(BeanFile file, ProjectProfile profile) {
        super(new FileTreeItem(profile, file));
        setShowRoot(true);
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
    }

    @Order(1)
    @Autowired
    public void nameColumn() {
        final TreeTableColumn<Object, String> column = new TreeTableColumn<>();
        column.textProperty().bind(ls("Name"));
        column.setCellValueFactory(f -> ((AbstractTreeItem<?>) f.getValue()).getName());
        column.setMinWidth(100);
        column.setPrefWidth(250);
        column.setMaxWidth(500);
        getColumns().add(column);
    }

    @Order(2)
    @Autowired
    public void typeColumn() {
        final TreeTableColumn<Object, String> column = new TreeTableColumn<>();
        column.textProperty().bind(ls("Type"));
        column.setCellValueFactory(f -> ((AbstractTreeItem<?>) f.getValue()).getType());
        column.setMinWidth(100);
        column.setPrefWidth(250);
        column.setMaxWidth(500);
        getColumns().add(column);
    }

    @Order(3)
    @Autowired
    public void valueColumn(SpecialActions specialActions) {
        final TreeTableColumn<Object, Object> column = new TreeTableColumn<>();
        column.textProperty().bind(ls("Value"));
        column.setCellValueFactory(f -> f.getValue().valueProperty());
        column.setCellFactory(f -> {
            final TreeTableCell<Object, Object> cell = new TreeTableCell<Object, Object>() {
                @Override
                protected void updateItem(Object item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        final AbstractTreeItem<?> treeItem = (AbstractTreeItem<?>) getTreeTableRow().getTreeItem();
                        setText(treeItem.getValue().toString());
                        setGraphic(treeItem.getValueGraphic());
                    }
                }
            };
            cell.setContextMenu(new MaridContextMenu(m -> {
                final AbstractTreeItem<?> treeItem = (AbstractTreeItem<?>) cell.getTreeTableRow().getTreeItem();
                m.getItems().clear();
                m.getItems().addAll(MaridActions.contextMenu(treeItem.actionMap));
            }));
            cell.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    final AbstractTreeItem<?> treeItem = (AbstractTreeItem<?>) cell.getTreeTableRow().getTreeItem();

                }
            });
            return cell;
        });
        column.setMinWidth(300);
        column.setPrefWidth(700);
        column.setMaxWidth(2000);
        getColumns().add(column);
    }
}
