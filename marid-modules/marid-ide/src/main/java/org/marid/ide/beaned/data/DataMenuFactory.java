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

import de.jensd.fx.glyphs.octicons.OctIcon;
import javafx.scene.control.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.*;

import static org.marid.jfx.icons.FontIcons.glyphIcon;

/**
 * @author Dmitry Ovchinnikov
 */
public class DataMenuFactory {

    public static ContextMenu contextMenu(TreeTableCell<Data, String> cell, BeanContext beanContext) {
        final TreeItem<Data> item = cell.getTreeTableRow().getTreeItem();
        if (item.getValue() instanceof BeanData) {
            return contextMenuBean(cell, beanContext);
        } else {
            return null;
        }
    }

    public static ContextMenu contextMenuBean(TreeTableCell<Data, String> cell, BeanContext beanContext) {
        final TreeItem<Data> item = cell.getTreeTableRow().getTreeItem();
        final BeanData beanData = (BeanData) item.getValue();
        final BeanInfo beanInfo = beanContext.beanInfo(beanData.getType());
        final ContextMenu contextMenu = new ContextMenu();
        final Map<Character, Set<MenuItem>> menuItemMap = new TreeMap<>();
        beanInfo.getConstructors().stream().min(Comparator.comparing(Constructor::getParameterCount)).ifPresent(c -> {
            for (final Parameter parameter : c.getParameters()) {
                final MenuItem menuItem = new MenuItem(parameter.getName(), glyphIcon(OctIcon.PACKAGE));
                menuItem.setOnAction(event -> {
                    if (item.getChildren().stream()
                            .map(TreeItem::getValue)
                            .filter(ConstructorArgData.class::isInstance)
                            .map(ConstructorArgData.class::cast)
                            .filter(ca -> parameter.getName().equals(ca.getName()))
                            .findAny()
                            .isPresent()) {
                        return;
                    }
                    final ConstructorArgData data = new ConstructorArgData(parameter.getType().getName(), parameter.getName());
                    final TreeItem<Data> treeItem = new TreeItem<>(data);
                    item.getChildren().add(treeItem);
                    item.setExpanded(true);
                });
                menuItemMap.computeIfAbsent('c', k -> new LinkedHashSet<>()).add(menuItem);
            }
        });
        beanInfo.getPropertyDescriptors().stream().filter(pd -> pd.getWriteMethod() != null).forEach(pd -> {
            final MenuItem menuItem = new MenuItem(pd.getName(), glyphIcon(OctIcon.STAR));
            menuItem.setOnAction(event -> {
                if (item.getChildren().stream()
                        .map(TreeItem::getValue)
                        .filter(PropertyArgData.class::isInstance)
                        .map(PropertyArgData.class::cast)
                        .filter(pa -> pd.getName().equals(pa.getName()))
                        .findAny()
                        .isPresent()) {
                    return;
                }
                final PropertyArgData data = new PropertyArgData(pd.getPropertyType().getName(), pd.getName());
                final TreeItem<Data> treeItem = new TreeItem<>(data);
                item.getChildren().add(treeItem);
                item.setExpanded(true);
            });
            menuItemMap.computeIfAbsent('p', k -> new LinkedHashSet<>()).add(menuItem);
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
