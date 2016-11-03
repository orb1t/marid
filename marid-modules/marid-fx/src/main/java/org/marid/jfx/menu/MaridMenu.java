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
import javafx.scene.control.*;
import org.marid.jfx.action.FxAction;

import java.util.Map;
import java.util.TreeMap;

import static org.marid.jfx.LocalizedStrings.ls;
import static org.marid.jfx.icons.FontIcons.glyphIcon;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridMenu extends MenuBar {

    public MaridMenu() {
        setMaxWidth(Double.MAX_VALUE);
    }

    public MaridMenu(Map<String, FxAction> actionMap) {
        this();
        init(actionMap);
    }

    protected void init(Map<String, FxAction> actionMap) {
        final Map<String, Map<String, Map<String, MenuItem>>> itemMap = new TreeMap<>();
        actionMap.forEach((id, action) -> {
            if (action.getGroup() == null) {
                return;
            }
            final MenuItem menuItem;
            if (action.selectedProperty() != null) {
                final CheckMenuItem checkMenuItem = new CheckMenuItem();
                checkMenuItem.selectedProperty().bindBidirectional(action.selectedProperty());
                menuItem = checkMenuItem;
            } else {
                menuItem = new MenuItem();
            }
            if (action.textProperty() != null) {
                menuItem.textProperty().bind(action.textProperty());
            }
            if (action.iconProperty() != null) {
                menuItem.graphicProperty().bind(Bindings.createObjectBinding(() -> {
                    if (action.getIcon() == null) {
                        return null;
                    } else {
                        return glyphIcon(action.getIcon(), 16);
                    }
                }, action.iconProperty()));
            }
            menuItem.setAccelerator(action.getAccelerator());
            menuItem.setOnAction(event -> action.getEventHandler().handle(event));
            if (action.disabledProperty() != null) {
                menuItem.disableProperty().bindBidirectional(action.disabledProperty());
            }
            itemMap
                    .computeIfAbsent(action.getMenu(), k -> new TreeMap<>())
                    .computeIfAbsent(action.getGroup(), k -> new TreeMap<>())
                    .put(action.getText(), menuItem);
        });
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
            getMenus().add(m);
        });
    }
}
