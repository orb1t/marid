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

package org.marid.jfx.menu;

import javafx.beans.binding.Bindings;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import org.marid.jfx.action.FxAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static org.marid.jfx.LocalizedStrings.ls;
import static org.marid.jfx.icons.FontIcons.glyphIcon;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
public interface MaridMenus {

    static List<Menu> menus(Map<String, FxAction> actionMap) {
        final Map<String, Map<String, Map<String, MenuItem>>> itemMap = new TreeMap<>();
        actionMap.forEach((id, action) -> {
            if (action.getMenu() == null) {
                return;
            }

            final MenuItem menuItem;
            if (action.selectedProperty() != null) {
                final CheckMenuItem checkMenuItem = new CheckMenuItem();
                checkMenuItem.selectedProperty().bindBidirectional(action.selectedProperty());
                menuItem = checkMenuItem;
            } else if (action.getChildren() != null) {
                final Menu menu = new Menu();
                final List<Menu> subMenus = menus(action.getChildren());
                switch (subMenus.size()) {
                    case 0:
                        return;
                    case 1:
                        menu.getItems().addAll(subMenus.get(0).getItems());
                        break;
                    default:
                        menu.getItems().addAll(subMenus);
                        break;
                }
                menuItem = menu;
            } else {
                menuItem = new MenuItem();
            }

            if (action.textProperty() != null) {
                menuItem.textProperty().bind(action.textProperty());
            }
            if (action.iconProperty() != null) {
                menuItem.graphicProperty().bind(Bindings.createObjectBinding(() -> {
                    final String icon = action.getIcon();
                    return icon != null ? glyphIcon(icon, 16) : null;
                }, action.iconProperty()));
            }
            if (action.acceleratorProperty() != null) {
                menuItem.acceleratorProperty().bind(action.acceleratorProperty());
            }

            if (!(menuItem instanceof Menu)) {
                menuItem.setOnAction(event -> action.getEventHandler().handle(event));
                if (action.disabledProperty() != null) {
                    menuItem.disableProperty().bindBidirectional(action.disabledProperty());
                }
            }
            itemMap
                    .computeIfAbsent(action.getMenu(), k -> new TreeMap<>())
                    .computeIfAbsent(action.getGroup(), k -> new TreeMap<>())
                    .put(id, menuItem);
        });
        final List<Menu> menus = new ArrayList<>();
        itemMap.forEach((menu, groupMap) -> {
            final Menu m = new Menu();
            m.textProperty().bind(ls(menu));
            groupMap.forEach((group, menuItems) -> {
                m.getItems().addAll(menuItems.values());
                m.getItems().add(new SeparatorMenuItem());
            });
            if (!m.getItems().isEmpty()) {
                m.getItems().remove(m.getItems().size() - 1);
            }
            menus.add(m);
        });
        return menus;
    }

    static MenuItem[] contextMenu(Map<String, FxAction> actionMap) {
        final AtomicBoolean first = new AtomicBoolean(true);
        return menus(actionMap).stream()
                .flatMap(m -> first.compareAndSet(true, false)
                        ? m.getItems().stream()
                        : Stream.concat(Stream.of(new SeparatorMenuItem()), m.getItems().stream()))
                .toArray(MenuItem[]::new);
    }
}
