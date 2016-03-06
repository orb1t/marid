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
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.marid.beans.MaridBeanXml;
import org.marid.ide.beaned.data.BeanContext;
import org.marid.ide.beaned.data.BeanData;
import org.marid.ide.beaned.data.Data;
import org.marid.jfx.ScrollPanes;
import org.marid.jfx.menu.MenuContainerBuilder;
import org.marid.l10n.L10nSupport;
import org.marid.logging.LogSupport;

import javax.lang.model.element.ElementKind;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.marid.ide.beaned.data.DataEditorFactory.newDialog;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanTreePane extends BorderPane implements LogSupport, L10nSupport {

    final BeanTree beanTree;

    public BeanTreePane(BeanContext beanContext) {
        setFocusTraversable(false);
        setCenter(ScrollPanes.scrollPane(beanTree = new BeanTree(beanContext)));
        final ToolBar toolBar = new ToolBar();
        final MenuBar menuBar = new MenuBar();
        final VBox vBox = new VBox(menuBar, toolBar);
        setTop(vBox);
        new MenuContainerBuilder()
                .menu("Beans", b -> b
                        .item("*Beans", MaterialIcon.ADD_BOX, contextMenu(beanContext))
                        .last(a -> a.getProperties().put("menu", contextMenu(beanContext)))
                        .item("*Clear all", MaterialIcon.CLEAR_ALL, event -> beanTree.getRoot().getChildren().clear())
                        .last(a -> a.disabledProperty().bind(Bindings.isEmpty(beanTree.getRoot().getChildren())))
                        .separator()
                        .item("*Edit...", MaterialDesignIcon.TABLE_EDIT, "F2", event -> beanTree.editItem(beanContext))
                        .last(a -> a.disabledProperty().bind(Bindings.createBooleanBinding(() -> {
                            final TreeItem<Data> treeItem = beanTree.getSelectionModel().getSelectedItem();
                            if (treeItem == null) {
                                return true;
                            }
                            final Dialog<Runnable> dialog = newDialog(beanTree, beanContext, treeItem.getValue());
                            return dialog == null;
                        }, beanTree.getSelectionModel().selectedIndexProperty()))))
                .build(menuBar.getMenus()::add, toolBar.getItems());
    }

    private ContextMenu contextMenu(BeanContext beanContext) {
        final ContextMenu contextMenu = new ContextMenu();
        final Pattern beanNamePattern = Pattern.compile("bean(\\d+)");
        for (final MaridBeanXml beanXml : beanContext.beansXmls) {
            if (beanXml.kind != ElementKind.CLASS) {
                continue;
            }
            final GlyphIcon<?> icon = BeanContext.icon(beanXml.icon, 16, OctIcon.CODE);
            final String text = beanXml.text == null ? beanXml.type : beanXml.text + ": " + beanXml.type;
            final MenuItem menuItem = new MenuItem(text, icon);
            final int beanNum = beanContext.root.getChildren().stream()
                    .map(TreeItem::getValue)
                    .filter(BeanData.class::isInstance)
                    .map(BeanData.class::cast)
                    .map(bd -> beanNamePattern.matcher(bd.getName()))
                    .filter(Matcher::matches)
                    .map(m -> m.group(1))
                    .mapToInt(Integer::parseInt)
                    .max()
                    .orElse(0) + 1;
            menuItem.setOnAction(event -> beanTree.addBean("bean" + beanNum, beanXml.type));
            contextMenu.getItems().add(menuItem);
        }
        return contextMenu;
    }
}
