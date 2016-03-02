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
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.octicons.OctIcon;
import javafx.beans.binding.Bindings;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.BorderPane;
import org.marid.beans.MaridBeanXml;
import org.marid.ide.beaned.data.BeanContext;
import org.marid.ide.beaned.data.Data;
import org.marid.jfx.ScrollPanes;
import org.marid.jfx.toolbar.ToolbarBuilder;
import org.marid.l10n.L10nSupport;
import org.marid.logging.LogSupport;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanTreePane extends BorderPane implements LogSupport, L10nSupport {

    final BeanTree beanTree;

    public BeanTreePane(BeanContext beanContext) {
        setCenter(ScrollPanes.scrollPane(beanTree = new BeanTree(beanContext)));
        setTop(new ToolbarBuilder()
                .add("Add bean", MaterialIcon.ADD_BOX, event -> {
                    final Node node = (Node) event.getSource();
                    contextMenu(beanContext).show(node, Side.BOTTOM, 0, 0);
                })
                .add("Clear all", MaterialIcon.CLEAR_ALL,
                        event -> beanTree.getRoot().getChildren().clear(),
                        b -> b.disableProperty().bind(Bindings.isEmpty(beanTree.getRoot().getChildren())))
                .addSeparator()
                .add("Edit name", MaterialIcon.MODE_EDIT,
                        event -> {
                            final TreeItem<Data> treeItem = beanTree.getSelectionModel().getSelectedItem();
                            beanTree.edit(beanTree.getRow(treeItem), beanTree.getTreeColumn());
                        },
                        b -> b.disableProperty().bind(Bindings.createBooleanBinding(() -> {
                            final TreeItem<Data> treeItem = beanTree.getSelectionModel().getSelectedItem();
                            return treeItem == null || !treeItem.getValue().isNameEditable();
                        }, beanTree.getSelectionModel().selectedIndexProperty())))
                .add("Edit value", MaterialDesignIcon.TABLE_EDIT,
                        event -> {},
                        b -> b.disableProperty().bind(Bindings.createBooleanBinding(() -> {
                            final TreeItem<Data> treeItem = beanTree.getSelectionModel().getSelectedItem();
                            return treeItem == null || !treeItem.getValue().isValueEditable();
                        }, beanTree.getSelectionModel().selectedIndexProperty())))
                .build());
    }

    private ContextMenu contextMenu(BeanContext beanContext) {
        final ContextMenu contextMenu = new ContextMenu();
        for (final MaridBeanXml beanXml : beanContext.beansXmls) {
            final GlyphIcon<?> icon = BeanContext.icon(beanXml.icon, 16, OctIcon.CODE);
            final String text = beanXml.text == null ? beanXml.type : beanXml.text + ": " + beanXml.type;
            final MenuItem menuItem = new MenuItem(text, icon);
            menuItem.setOnAction(event -> beanTree.addBean("bean1", beanXml.type));
            contextMenu.getItems().add(menuItem);
        }
        return contextMenu;
    }
}
