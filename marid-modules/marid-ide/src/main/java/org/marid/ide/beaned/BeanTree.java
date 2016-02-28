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

import de.jensd.fx.glyphs.GlyphIcon;
import de.jensd.fx.glyphs.octicons.OctIcon;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import org.marid.l10n.L10nSupport;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanTree extends TreeTableView<BeanTreeItem> implements L10nSupport {

    public BeanTree(BeanEditorPane editorPane) {
        super(new TreeItem<>(new BeanTreeItem(BeanTreeItemType.ROOT)));
        setShowRoot(false);
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        final TreeTableColumn<BeanTreeItem, String> nameColumn = nameColumn();
        getColumns().add(nameColumn);
        getColumns().add(typeColumn(editorPane));
        getColumns().add(valueColumn());
        setTreeColumn(nameColumn());
    }

    private TreeTableColumn<BeanTreeItem, String> nameColumn() {
        final TreeTableColumn<BeanTreeItem, String> column = new TreeTableColumn<>(s("Name"));
        column.setEditable(true);
        column.setMinWidth(70);
        column.setPrefWidth(70);
        column.setMaxWidth(150);
        column.setCellValueFactory(param -> param.getValue().getValue().nameProperty);
        return column;
    }

    private TreeTableColumn<BeanTreeItem, BeanTreeItem> typeColumn(BeanEditorPane editorPane) {
        final TreeTableColumn<BeanTreeItem, BeanTreeItem> column = new TreeTableColumn<>(s("Type"));
        column.setEditable(false);
        column.setMinWidth(100);
        column.setPrefWidth(150);
        column.setMaxWidth(400);
        column.setCellFactory(param -> new TreeTableCell<BeanTreeItem, BeanTreeItem>() {
            @Override
            protected void updateItem(BeanTreeItem item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    final String iconName = editorPane.beanBrowserPane.beanBrowser.icon(item.getType());
                    final GlyphIcon<?> icon = BeanBrowser.icon(iconName, 16, OctIcon.BOOK);
                    setGraphic(icon);
                }
            }
        });
        return column;
    }

    private TreeTableColumn<BeanTreeItem, String> valueColumn() {
        final TreeTableColumn<BeanTreeItem, String> column = new TreeTableColumn<>(s("Value"));
        column.setEditable(false);
        column.setMinWidth(200);
        column.setPrefWidth(300);
        column.setMaxWidth(900);
        column.setCellValueFactory(param -> param.getValue().getValue().valueProperty);
        return column;
    }
}
