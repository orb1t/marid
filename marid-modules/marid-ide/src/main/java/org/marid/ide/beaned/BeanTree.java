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

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.octicons.OctIcon;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import org.marid.ide.beaned.data.*;
import org.marid.ide.timers.IdeTimers;
import org.marid.jfx.icons.FontIcons;
import org.marid.jfx.table.MaridTreeTableViewSkin;
import org.marid.l10n.L10nSupport;
import org.marid.logging.LogSupport;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.marid.ide.beaned.data.DataEditorFactory.newDialog;
import static org.marid.jfx.icons.FontIcons.glyphIcon;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanTree extends TreeTableView<Data> implements L10nSupport, LogSupport {

    private final BeanContext beanContext;
    private final MaridTreeTableViewSkin<Data> skin;
    private final IdeTimers ideTimers;

    public BeanTree(BeanContext beanContext, IdeTimers ideTimers) {
        super(beanContext.root);
        this.beanContext = beanContext;
        this.ideTimers = ideTimers;
        setShowRoot(false);
        setColumnResizePolicy(UNCONSTRAINED_RESIZE_POLICY);
        setSkin(skin = new MaridTreeTableViewSkin<>(this));
        getColumns().add(nameColumn());
        getColumns().add(typeColumn());
        getColumns().add(valueColumn());
        setEditable(false);
        setSortMode(TreeSortMode.ONLY_FIRST_LEVEL);
        setCache(false);
    }

    public BeanContext getBeanContext() {
        return beanContext;
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        ideTimers.delayed(300L, task -> Platform.runLater(() -> {
            for (final TreeTableColumn<Data, ?> c : getColumns()) {
                skin.resizeColumnToFitContent(c, -1);
            }
            skin.refresh();
        }));
    }

    private TreeTableColumn<Data, String> nameColumn() {
        final TreeTableColumn<Data, String> column = new TreeTableColumn<>(s("Name"));
        column.setResizable(false);
        column.setCellValueFactory(param -> param.getValue().getValue().nameProperty());
        column.setCellFactory(param -> new TreeTableCell<Data, String>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                setGraphic(null);
                if (item == null || empty) {
                    return;
                }
                final TreeItem<Data> treeItem = getTreeTableRow().getTreeItem();
                if (treeItem == null) {
                    return;
                }
                setText(item);
                setGraphic(FontIcons.glyphIcon(treeItem.getValue().getIcon(), 16));
                final ContextMenu contextMenu = new ContextMenu();
                final MenuItem editMenuItem = new MenuItem(LS.s("Edit..."), glyphIcon(MaterialDesignIcon.TABLE_EDIT));
                editMenuItem.setAccelerator(KeyCombination.valueOf("F4"));
                editMenuItem.setOnAction(event -> editItem());
                contextMenu.getItems().add(editMenuItem);
                DataMenuFactory.contextMenu(BeanTree.this, this, contextMenu);
                setContextMenu(contextMenu);
            }
        });
        return column;
    }

    private TreeTableColumn<Data, String> typeColumn() {
        final TreeTableColumn<Data, String> column = new TreeTableColumn<>(s("Type"));
        column.setResizable(false);
        column.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue().getType()));
        column.setCellFactory(param -> new TreeTableCell<Data, String>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                setGraphic(null);
                if (item == null || empty) {
                    return;
                }
                final TreeItem<Data> treeItem = getTreeTableRow().getTreeItem();
                if (treeItem == null) {
                    return;
                }
                setText(item);
                final String iconName = beanContext.icon(item);
                setGraphic(BeanContext.icon(iconName, 16, OctIcon.PRIMITIVE_SQUARE));
            }
        });
        return column;
    }

    private TreeTableColumn<Data, String> valueColumn() {
        final TreeTableColumn<Data, String> column = new TreeTableColumn<>(s("Value"));
        column.setResizable(false);
        column.setCellValueFactory(param -> param.getValue().getValue().valueProperty());
        column.setCellFactory(param -> new TreeTableCell<Data, String>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                setGraphic(null);
                if (item == null || empty) {
                    return;
                }
                final TreeItem<Data> treeItem = getTreeTableRow().getTreeItem();
                if (treeItem == null) {
                    return;
                }
                final Node graphic = DataGraphicFactory.getGraphic(BeanTree.this, treeItem.getValue());
                setGraphic(graphic);
            }
        });
        return column;
    }

    public String newBeanName() {
        final Pattern beanNamePattern = Pattern.compile("bean(\\d+)");
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
        return "bean" + beanNum;
    }

    public TreeItem<Data> addBean(String name, String type) {
        final BeanData beanData = new BeanData(type, name);
        final TreeItem<Data> treeItem = new TreeItem<>(beanData);
        getRoot().getChildren().add(treeItem);
        return treeItem;
    }

    public TreeItem<Data> addBean(String name, String type, StringProperty factoryBean, String factoryMethod) {
        final BeanData beanData = new BeanData(type, name);
        beanData.factoryBeanProperty().bind(factoryBean);
        beanData.factoryMethodProperty().set(factoryMethod);
        final TreeItem<Data> treeItem = new TreeItem<>(beanData);
        getRoot().getChildren().add(treeItem);
        return treeItem;
    }

    public void editItem() {
        final TreeItem<Data> treeItem = getSelectionModel().getSelectedItem();
        final Dialog<Runnable> dialog = newDialog(this, beanContext, treeItem.getValue());
        if (dialog != null) {
            dialog.showAndWait().ifPresent(Runnable::run);
        }
    }
}
