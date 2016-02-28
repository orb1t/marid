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

package org.marid.ide.menu;

import javafx.scene.Node;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;

import java.util.function.BiFunction;

/**
 * @author Dmitry Ovchinnikov
 */
public enum MenuItemType {

    NORMAL(MenuItem::new),
    CHECK(CheckMenuItem::new);

    private final BiFunction<String, Node, ? extends MenuItem> menuItemProvider;

    MenuItemType(BiFunction<String, Node, ? extends MenuItem> menuItemProvider) {
        this.menuItemProvider = menuItemProvider;
    }

    public MenuItem createItem(String text, Node graphic) {
        return menuItemProvider.apply(text, graphic);
    }
}
