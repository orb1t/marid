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

package org.marid.ide.swing.impl;

import org.marid.ide.menu.MenuEntry;
import org.marid.util.CollectUtils;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

/**
 * @author Dmitry Ovchinnikov
 */
class MenuBarImpl extends JMenuBar {

    public MenuBarImpl(List<MenuEntry> menuEntries) {
        TreeSet<MenuEntry> set = new TreeSet<>(MenuEntry.MENU_ENTRY_COMPARATOR);
        for (MenuEntry e : menuEntries) {
            if (e.getPath().length == 0) {
                set.add(e);
            }
        }
        for (MenuEntry e : set) {
            switch (e.getType()) {
                case ITEM:
                    add(getItem(e));
                    break;
                case CHECKED:
                    add(getCheckedItem(e));
                    break;
                case RADIO:
                    add(getRadioItem(e));
                    break;
                case MENU:
                    add(getMenu(menuEntries, e));
                    break;
            }
        }
    }

    private JMenuItem getItem(MenuEntry entry) {
        return new JMenuItem(new MenuActionImpl(entry));
    }

    private JRadioButtonMenuItem getRadioItem(MenuEntry entry) {
        return new JRadioButtonMenuItem(new MenuActionImpl(entry));
    }

    private JCheckBoxMenuItem getCheckedItem(MenuEntry entry) {
        return new JCheckBoxMenuItem(new MenuActionImpl(entry));
    }

    private JMenu getMenu(List<MenuEntry> menuEntries, MenuEntry entry) {
        JMenu menu = new JMenu(new MenuActionImpl(entry)) {
            @Override
            public void setPopupMenuVisible(boolean b) {
                super.setPopupMenuVisible(b);
                for (Component c : getMenuComponents()) {
                    if (c instanceof JMenuItem) {
                        if (((JMenuItem) c).getAction() instanceof MenuActionImpl) {
                            ((MenuActionImpl)((JMenuItem) c).getAction()).update();
                        }
                    }
                }
            }
        };
        String[] path = entry.getPath();
        int n = path.length;
        TreeSet<MenuEntry> set = new TreeSet<>(MenuEntry.MENU_ENTRY_COMPARATOR);
        for (MenuEntry e : menuEntries) {
            if (e.getPath().length == n + 1 && Arrays.equals(e.getPath(),
                    CollectUtils.concat(entry.getPath(), entry.getName()))) {
                set.add(e);
            }
        }
        for (MenuEntry e : set) {
            switch (e.getType()) {
                case ITEM:
                    menu.add(getItem(e));
                    break;
                case CHECKED:
                    menu.add(getCheckedItem(e));
                    break;
                case RADIO:
                    menu.add(getRadioItem(e));
                    break;
                case MENU:
                    menu.add(getMenu(menuEntries, e));
                    break;
            }
        }
        return menu;
    }
}