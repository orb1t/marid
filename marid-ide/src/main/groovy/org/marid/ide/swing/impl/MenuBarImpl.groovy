/*
 * Copyright (C) 2013 Dmitry Ovchinnikov
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

package org.marid.ide.swing.impl

import org.marid.ide.menu.MenuEntry
import org.marid.ide.menu.MenuType

import javax.swing.*

/**
 * Menu bar.
 *
 * @author Dmitry Ovchinnikov 
 */
class MenuBarImpl extends JMenuBar {
    MenuBarImpl(List<MenuEntry> menuEntries) {
        for (def entry in menuEntries
                .findAll{it.path.length == 0}
                .sort(false, {e1, e2 -> e1.priority <=> e2.priority})) {
            switch (entry.type) {
                case MenuType.ITEM:
                    add(getItem(entry));
                    break;
                case MenuType.CHECKED:
                    add(getCheckedItem(entry));
                    break;
                case MenuType.RADIO:
                    add(getRadioItem(entry));
                    break;
                case MenuType.MENU:
                    add(getMenu(menuEntries, entry));
                    break;
            }
        }
    }

    private def getItem(MenuEntry entry) {
        return new JMenuItem(new MenuActionImpl(entry));
    }

    private def getRadioItem(MenuEntry entry) {
        return new JRadioButtonMenuItem(new MenuActionImpl(entry));
    }

    private def getCheckedItem(MenuEntry entry) {
        return new JCheckBoxMenuItem(new MenuActionImpl(entry));
    }

    private def getMenu(List<MenuEntry> menuEntries, MenuEntry entry) {
        def JMenu menu = new JMenu(new MenuActionImpl(entry)) {
            @Override
            void setPopupMenuVisible(boolean b) {
                super.setPopupMenuVisible(b)
                for (def c in menuComponents) {
                    if (c instanceof JMenuItem) {
                        if (c.action instanceof MenuActionImpl) {
                            ((MenuActionImpl)c.action).update();
                        }
                    }
                }
            }
        };
        def path = entry.path;
        def n = path.length;
        def v = menuEntries.findAll{it.path.length == n + 1 && it.path == entry.path + entry.name};
        for (def e in v.sort(false, {e1, e2 -> e1.priority <=> e2.priority})) {
            switch (e.type) {
                case MenuType.ITEM:
                    menu.add(getItem(e));
                    break;
                case MenuType.CHECKED:
                    menu.add(getCheckedItem(e));
                    break;
                case MenuType.RADIO:
                    menu.add(getRadioItem(e));
                    break;
                case MenuType.MENU:
                    menu.add(getMenu(menuEntries, e));
                    break;
            }
        }
        return menu;
    }
}
