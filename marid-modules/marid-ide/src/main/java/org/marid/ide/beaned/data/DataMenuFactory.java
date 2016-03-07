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

package org.marid.ide.beaned.data;

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.octicons.OctIcon;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import org.marid.ide.beaned.BeanTree;
import org.marid.l10n.L10nSupport;

import javax.lang.model.element.ElementKind;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.*;

import static org.marid.ide.beaned.data.DataEditorFactory.newDialog;
import static org.marid.jfx.icons.FontIcons.glyphIcon;

/**
 * @author Dmitry Ovchinnikov
 */
public class DataMenuFactory implements L10nSupport {

    public static ContextMenu contextMenu(BeanTree beanTree, TreeTableCell<Data, String> cell, BeanContext beanContext) {
        final TreeItem<Data> item = cell.getTreeTableRow().getTreeItem();
        if (item.getValue() instanceof BeanData) {
            return contextMenuBean(beanTree, cell, beanContext);
        } else {
            return null;
        }
    }

    public static boolean isPresent(String name, Class<? extends Data> dataClass, TreeItem<Data> item) {
        return item.getChildren().stream()
                .map(TreeItem::getValue)
                .filter(dataClass::isInstance)
                .filter(e -> name.equals(e.getName()))
                .findAny()
                .isPresent();
    }

    static ContextMenu contextMenuBean(BeanTree beanTree, TreeTableCell<Data, String> cell, BeanContext beanContext) {
        final TreeItem<Data> item = cell.getTreeTableRow().getTreeItem();
        final BeanData beanData = (BeanData) item.getValue();
        final BeanInfo beanInfo = beanContext.beanInfo(beanData.getType());
        final ContextMenu contextMenu = new ContextMenu();
        final Map<Character, Set<MenuItem>> menuItemMap = new TreeMap<>();
        beanInfo.getConstructors().stream().min(Comparator.comparing(Constructor::getParameterCount)).ifPresent(c -> {
            for (final Parameter parameter : c.getParameters()) {
                final MenuItem menuItem = new MenuItem(parameter.getName(), glyphIcon(OctIcon.PACKAGE));
                menuItem.setOnAction(event -> {
                    if (isPresent(parameter.getName(), ConstructorArgData.class, item)) {
                        return;
                    }
                    final ConstructorArgData data = new ConstructorArgData(parameter.getType().getName(), parameter.getName());
                    final TreeItem<Data> treeItem = new TreeItem<>(data);
                    item.getChildren().add(treeItem);
                    item.setExpanded(true);
                    beanTree.getSelectionModel().select(treeItem);
                });
                menuItemMap.computeIfAbsent('c', k -> new LinkedHashSet<>()).add(menuItem);
            }
        });
        beanInfo.getPropertyDescriptors().stream().filter(pd -> pd.getWriteMethod() != null).forEach(pd -> {
            final MenuItem menuItem = new MenuItem(pd.getName(), glyphIcon(OctIcon.STAR));
            menuItem.setOnAction(event -> {
                if (isPresent(pd.getName(), PropertyArgData.class, item)) {
                    return;
                }
                final PropertyArgData data = new PropertyArgData(pd.getPropertyType().getName(), pd.getName());
                final TreeItem<Data> treeItem = new TreeItem<>(data);
                item.getChildren().add(treeItem);
                item.setExpanded(true);
                beanTree.getSelectionModel().select(treeItem);
            });
            menuItemMap.computeIfAbsent('p', k -> new LinkedHashSet<>()).add(menuItem);
        });
        if (newDialog((BeanTree) cell.getTreeTableView(), beanContext, cell.getTreeTableRow().getItem()) != null) {
            final MenuItem editMenuItem = new MenuItem(LS.s("Edit..."), glyphIcon(MaterialDesignIcon.TABLE_EDIT));
            editMenuItem.setAccelerator(KeyCombination.valueOf("F2"));
            editMenuItem.setOnAction(event -> ((BeanTree) cell.getTreeTableView()).editItem(beanContext));
            menuItemMap.computeIfAbsent('e', k -> new LinkedHashSet<>()).add(editMenuItem);
        }
        beanContext.beansXmls.stream().filter(xml -> xml.kind == ElementKind.METHOD).forEach(xml -> {
            final BeanInfo parentBeanInfo = beanContext.beanInfo(xml.parent);
            if (parentBeanInfo.getType().isAssignableFrom(beanInfo.getType())) {
                final MenuItem menuItem = new MenuItem(xml.text, glyphIcon(OctIcon.BELL));
                menuItem.setOnAction(event -> beanTree.addBean(beanTree.newBeanName(beanContext), xml.type, beanData.getName(), xml.text));
                menuItemMap.computeIfAbsent('n', k -> new LinkedHashSet<>()).add(menuItem);
            }
        });
        menuItemMap.forEach((g, items) -> {
            if (!contextMenu.getItems().isEmpty()) {
                contextMenu.getItems().add(new SeparatorMenuItem());
            }
            contextMenu.getItems().addAll(items);
        });
        return contextMenu;
    }
}
