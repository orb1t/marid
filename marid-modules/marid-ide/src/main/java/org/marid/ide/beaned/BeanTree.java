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
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.util.converter.DefaultStringConverter;
import org.marid.l10n.L10nSupport;
import org.marid.logging.LogSupport;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Optional;
import java.util.stream.Stream;

import static org.marid.ide.beaned.BeanTreeItemType.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanTree extends TreeTableView<BeanTreeItem> implements L10nSupport, LogSupport {

    public BeanTree(BeanEditorPane editorPane) {
        super(new TreeItem<>(new BeanTreeItem(BeanTreeItemType.ROOT)));
        setShowRoot(false);
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        final TreeTableColumn<BeanTreeItem, String> nameColumn = nameColumn(editorPane);
        getColumns().add(nameColumn);
        getColumns().add(valueColumn());
        setTreeColumn(nameColumn);
        setEditable(true);
    }

    private TreeTableColumn<BeanTreeItem, String> nameColumn(BeanEditorPane editorPane) {
        final TreeTableColumn<BeanTreeItem, String> column = new TreeTableColumn<>(s("Name"));
        column.setMinWidth(200);
        column.setPrefWidth(270);
        column.setMaxWidth(550);
        column.setCellValueFactory(param -> param.getValue().getValue().nameProperty);
        column.setCellFactory(param -> new TextFieldTreeTableCell<BeanTreeItem, String>(new DefaultStringConverter()) {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    return;
                }
                final TreeItem<BeanTreeItem> treeItem = getTreeTableRow().getTreeItem();
                if (treeItem == null) {
                    return;
                }
                setText(item + ": " + treeItem.getValue().getType());
                setContextMenu(contextMenu(treeItem, editorPane));
                setEditable(treeItem.getValue().itemType.isNameEditable());
            }
        });
        return column;
    }

    private TreeTableColumn<BeanTreeItem, String> valueColumn() {
        final TreeTableColumn<BeanTreeItem, String> column = new TreeTableColumn<>(s("Value"));
        column.setMinWidth(250);
        column.setPrefWidth(390);
        column.setMaxWidth(1000);
        column.setCellValueFactory(param -> param.getValue().getValue().valueProperty);
        column.setCellFactory(param -> new TextFieldTreeTableCell<BeanTreeItem, String>(new DefaultStringConverter()) {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    return;
                }
                final TreeItem<BeanTreeItem> treeItem = getTreeTableRow().getTreeItem();
                if (treeItem == null) {
                    return;
                }
                setEditable(treeItem.getValue().itemType.isValueEditable());
            }
        });
        return column;
    }

    TreeItem<BeanTreeItem> addBean(String name, String type, String icon) {
        final BeanTreeItem beanTreeItem = new BeanTreeItem(BEAN, name, type);
        final TreeItem<BeanTreeItem> treeItem = new TreeItem<>(beanTreeItem, BeanTreePane.icon(icon, 16, BEAN.getIcon()));
        getRoot().getChildren().add(treeItem);
        return treeItem;
    }

    ContextMenu contextMenu(TreeItem<BeanTreeItem> treeItem, BeanEditorPane editorPane) {
        final ContextMenu contextMenu = new ContextMenu();
        try {
            switch (treeItem.getValue().itemType) {
                case BEAN:
                    fillBeanMenu(treeItem, editorPane, contextMenu);
                    break;
            }
        } catch (Exception x) {
            log(WARNING, "Context menu creation error", x);
        }
        return contextMenu;
    }

    void fillBeanMenu(TreeItem<BeanTreeItem> item, BeanEditorPane editorPane, ContextMenu menu) throws Exception {
        final BeanTreeItem beanTreeItem = item.getValue();
        final Class<?> beanClass = Class.forName(beanTreeItem.getType(), false, editorPane.classLoader);
        final Optional<Constructor<?>> constructorOptional = Stream.of(beanClass.getConstructors()).findAny();
        if (constructorOptional.isPresent()) {
            for (final Parameter p : constructorOptional.get().getParameters()) {
                final BeanTreeItem arg = new BeanTreeItem(CONSTRUCTOR_ARG, p.getName(), p.getType().getName());
                final String iconName = editorPane.beanTreePane.icon(p.getType().getName());
                final GlyphIcon<?> icon = BeanTreePane.icon(iconName, 16, CONSTRUCTOR_ARG.getIcon());
                final MenuItem menuItem = new MenuItem(p.getName(), icon);
                menuItem.setOnAction(event -> {
                    final TreeItem<BeanTreeItem> child = new TreeItem<>(arg, icon);
                    item.getChildren().add(child);
                    item.setExpanded(true);
                });
                menu.getItems().add(menuItem);
            }
        }
        Introspector.flushCaches();
        final BeanInfo beanInfo = Introspector.getBeanInfo(beanClass);
        final PropertyDescriptor[] propertyDescriptors = Stream.of(beanInfo.getPropertyDescriptors())
                .filter(p -> p.getWriteMethod() != null)
                .toArray(PropertyDescriptor[]::new);
        if (propertyDescriptors.length > 0) {
            if (!menu.getItems().isEmpty()) {
                menu.getItems().add(new SeparatorMenuItem());
            }
            for (final PropertyDescriptor p : propertyDescriptors) {
                final BeanTreeItem arg = new BeanTreeItem(PROPERTY, p.getName(), p.getPropertyType().getName());
                final String iconName = editorPane.beanTreePane.icon(p.getPropertyType().getName());
                final GlyphIcon<?> icon = BeanTreePane.icon(iconName, 16, PROPERTY.getIcon());
                final MenuItem menuItem = new MenuItem(p.getName(), icon);
                menuItem.setOnAction(event -> {
                    final TreeItem<BeanTreeItem> child = new TreeItem<>(arg, icon);
                    item.getChildren().add(child);
                    item.setExpanded(true);
                });
                menu.getItems().add(menuItem);
            }
        }
        final Method[] methods = Stream.of(beanClass.getMethods())
                .filter(m -> !m.getReturnType().isPrimitive())
                .filter(m -> !"getClass".equals(m.getName()))
                .filter(m -> !"toString".equals(m.getName()))
                .toArray(Method[]::new);
        if (methods.length > 0) {
            if (!menu.getItems().isEmpty()) {
                menu.getItems().add(new SeparatorMenuItem());
            }
            for (final Method m : methods) {
                final String iconName = editorPane.beanTreePane.icon(m.getReturnType().getName());
                final GlyphIcon<?> icon = BeanTreePane.icon(iconName, 16, PROPERTY.getIcon());
                final MenuItem menuItem = new MenuItem(s("Add bean from %s", m.getName()), icon);
                menuItem.setOnAction(event -> {
                    addBean(m.getName(), m.getReturnType().getName(), iconName);
                });
                menu.getItems().add(menuItem);
            }
        }
    }
}
