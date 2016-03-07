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
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.*;
import org.marid.ide.beaned.data.BeanContext;
import org.marid.ide.beaned.data.BeanData;
import org.marid.ide.beaned.data.Data;
import org.marid.ide.beaned.data.DataMenuFactory;
import org.marid.ide.timers.IdeTimers;
import org.marid.jfx.icons.FontIcons;
import org.marid.jfx.table.MaridTreeTableViewSkin;
import org.marid.l10n.L10nSupport;
import org.marid.logging.LogSupport;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.marid.ide.beaned.data.DataEditorFactory.newDialog;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanTree extends TreeTableView<Data> implements L10nSupport, LogSupport {

    public BeanTree(BeanContext beanContext, IdeTimers ideTimers) {
        super(beanContext.root);
        setShowRoot(false);
        setColumnResizePolicy(UNCONSTRAINED_RESIZE_POLICY);
        final MaridTreeTableViewSkin<Data> skin = new MaridTreeTableViewSkin<>(this);
        setSkin(skin);
        getColumns().add(nameColumn(beanContext));
        getColumns().add(typeColumn(beanContext));
        getColumns().add(valueColumn(beanContext));
        setEditable(false);
        setSortMode(TreeSortMode.ONLY_FIRST_LEVEL);
        final AtomicBoolean dirty = new AtomicBoolean();
        ideTimers.with(this, () -> ideTimers.schedule(300L, task -> {
            if (dirty.get()) {
                Platform.runLater(() -> {
                    for (final TreeTableColumn<Data, ?> c : getColumns()) {
                        skin.resizeColumnToFitContent(c, -1);
                    }
                    skin.refresh();
                    dirty.set(false);
                });
            }
        }));
        needsLayoutProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                dirty.compareAndSet(false, true);
            }
        });
    }

    private TreeTableColumn<Data, String> nameColumn(BeanContext beanContext) {
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
                setContextMenu(DataMenuFactory.contextMenu(BeanTree.this, this, beanContext));
            }
        });
        return column;
    }

    private TreeTableColumn<Data, String> typeColumn(BeanContext context) {
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
                final String iconName = context.icon(item);
                setGraphic(BeanContext.icon(iconName, 16, OctIcon.PRIMITIVE_SQUARE));
            }
        });
        return column;
    }

    private TreeTableColumn<Data, String> valueColumn(BeanContext beanContext) {
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

    public void editItem(BeanContext beanContext) {
        final TreeItem<Data> treeItem = getSelectionModel().getSelectedItem();
        final Dialog<Runnable> dialog = newDialog(this, beanContext, treeItem.getValue());
        if (dialog != null) {
            dialog.showAndWait().ifPresent(Runnable::run);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        System.out.println(1);
        super.finalize();
    }
}
