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

import javafx.beans.binding.Bindings;
import javafx.scene.control.*;
import org.marid.dependant.beaneditor.ValueMenuItems;
import org.marid.dependant.beantree.items.AbstractTreeItem;
import org.marid.dependant.beantree.items.ProjectTreeItem;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.action.MaridActions;
import org.marid.jfx.menu.MaridContextMenu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import static org.marid.jfx.LocalizedStrings.ls;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanTree extends TreeTableView<Object> {

    @Autowired
    public BeanTree(ProjectProfile profile, AutowireCapableBeanFactory beanFactory) {
        super(new ProjectTreeItem(profile));
        setShowRoot(true);
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        beanFactory.initializeBean(getRoot(), null);
        beanFactory.autowireBean(getRoot());
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
    public void valueColumn() {
        final TreeTableColumn<Object, TreeItem<Object>> column = new TreeTableColumn<>();
        column.textProperty().bind(ls("Value"));
        column.setCellValueFactory(f -> Bindings.createObjectBinding(f::getValue));
        column.setCellFactory(f -> new TreeTableCell<Object, TreeItem<Object>>() {
            @Override
            protected void updateItem(TreeItem<Object> item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    textProperty().unbind();
                    graphicProperty().unbind();
                    setText(null);
                    setGraphic(null);
                } else {
                    final AbstractTreeItem<?> treeItem = (AbstractTreeItem<?>) item;
                    textProperty().bind(treeItem.valueText());
                    graphicProperty().bind(treeItem.valueGraphic());
                }
            }
        });
        column.setMinWidth(300);
        column.setPrefWidth(700);
        column.setMaxWidth(2000);
        getColumns().add(column);
    }

    @Autowired
    private void initRow() {
        setRowFactory(v -> {
            final TreeTableRow<Object> row = new TreeTableRow<>();
            row.focusedProperty().addListener((observable, oldValue, newValue) -> {
                final AbstractTreeItem<?> treeItem = (AbstractTreeItem<?>) row.getTreeItem();
                if (treeItem != null) {
                    treeItem.focused.set(newValue);
                }
            });
            row.setContextMenu(new MaridContextMenu(m -> {
                final AbstractTreeItem<?> treeItem = (AbstractTreeItem<?>) row.getTreeItem();
                m.getItems().clear();
                if (treeItem == null) {
                    return;
                }
                m.getItems().addAll(MaridActions.contextMenu(treeItem.actionMap));
                final ValueMenuItems valueMenuItems = treeItem.valueMenuItems.get();
                if (valueMenuItems != null) {
                    valueMenuItems.addTo(m);
                }
            }));
            row.setLineSpacing(1.0);
            return row;
        });
    }
}
