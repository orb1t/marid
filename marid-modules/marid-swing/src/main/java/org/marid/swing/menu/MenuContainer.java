/*
 * Copyright (C) 2014 Dmitry Ovchinnikov
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

package org.marid.swing.menu;

import java.awt.*;

import static java.util.stream.IntStream.range;

/**
 * @author Dmitry Ovchinnikov.
 */
public class MenuContainer implements AwtMenuContainer {

    private final Menu menu;

    public MenuContainer(Menu menu) {
        this.menu = menu;
    }

    @Override
    public Menu getMenu(int index) {
        final int n = menu.getItemCount();
        return (Menu) range(0, n).mapToObj(menu::getItem).filter(Menu.class::isInstance).skip(index).findFirst().get();
    }

    @Override
    public int getMenuCount() {
        return (int) range(0, menu.getItemCount()).mapToObj(menu::getItem).filter(Menu.class::isInstance).count();
    }

    @Override
    public void add(MenuItem menuItem) {
        menu.add(menuItem);
    }

    @Override
    public void addSeparator() {
        menu.addSeparator();
    }
}
