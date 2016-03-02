/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
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

package org.marid.ide.beaned;

import de.jensd.fx.glyphs.octicons.OctIcon;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.util.converter.DefaultStringConverter;
import org.marid.ide.beaned.data.BeanContext;
import org.marid.ide.beaned.data.BeanData;
import org.marid.ide.beaned.data.Data;
import org.marid.jfx.icons.FontIcons;
import org.marid.l10n.L10nSupport;
import org.marid.logging.LogSupport;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanTree extends TreeTableView<Data> implements L10nSupport, LogSupport {

    public BeanTree(BeanContext beanContext) {
        super(beanContext.root);
        setShowRoot(false);
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        final TreeTableColumn<Data, String> nameColumn = nameColumn(beanContext);
        getColumns().add(nameColumn);
        getColumns().add(typeColumn(beanContext));
        getColumns().add(valueColumn());
        setTreeColumn(nameColumn);
        setEditable(true);
    }

    private TreeTableColumn<Data, String> nameColumn(BeanContext beanContext) {
        final TreeTableColumn<Data, String> column = new TreeTableColumn<>(s("Name"));
        column.setMinWidth(100);
        column.setPrefWidth(120);
        column.setMaxWidth(300);
        column.setCellValueFactory(param -> param.getValue().getValue().nameProperty());
        column.setCellFactory(param -> new TextFieldTreeTableCell<Data, String>(new DefaultStringConverter()) {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    return;
                }
                final TreeItem<Data> treeItem = getTreeTableRow().getTreeItem();
                if (treeItem == null) {
                    return;
                }
                setText(item);
                setEditable(treeItem.getValue().isNameEditable());
                setGraphic(FontIcons.glyphIcon(treeItem.getValue().getIcon(), 16));
            }
        });
        return column;
    }

    private TreeTableColumn<Data, String> typeColumn(BeanContext context) {
        final TreeTableColumn<Data, String> column = new TreeTableColumn<>(s("Type"));
        column.setMinWidth(200);
        column.setPrefWidth(270);
        column.setMaxWidth(550);
        column.setEditable(false);
        column.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue().getType()));
        column.setCellFactory(param -> new TreeTableCell<Data, String>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    return;
                }
                final TreeItem<Data> treeItem = getTreeTableRow().getTreeItem();
                if (treeItem == null) {
                    return;
                }
                setText(item);
                final String iconName = context.icon(item);
                setGraphic(BeanContext.icon(iconName, 16, OctIcon.PRIMITIVE_SQUARE));
            }
        });
        return column;
    }

    private TreeTableColumn<Data, String> valueColumn() {
        final TreeTableColumn<Data, String> column = new TreeTableColumn<>(s("Value"));
        column.setMinWidth(250);
        column.setPrefWidth(390);
        column.setMaxWidth(1000);
        column.setCellValueFactory(param -> param.getValue().getValue().valueProperty());
        column.setEditable(false);
        column.setCellFactory(param -> new TreeTableCell<Data, String>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    return;
                }
                final TreeItem<Data> treeItem = getTreeTableRow().getTreeItem();
                if (treeItem == null) {
                    return;
                }
            }
        });
        return column;
    }

    TreeItem<Data> addBean(String name, String type) {
        final BeanData beanData = new BeanData(type, name);
        final TreeItem<Data> treeItem = new TreeItem<>(beanData);
        getRoot().getChildren().add(treeItem);
        return treeItem;
    }
}
