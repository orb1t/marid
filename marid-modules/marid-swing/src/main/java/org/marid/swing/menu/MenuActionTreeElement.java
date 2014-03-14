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

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.TreeMap;

import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
public class MenuActionTreeElement {

    public final MenuActionTreeElement parent;
    public final String name;
    public final Action action;
    public final Map<String, Object> properties;
    public final TreeMap<String, TreeMap<String, MenuActionTreeElement>> children = new TreeMap<>();

    public MenuActionTreeElement(MenuActionTreeElement parent, MenuAction action) {
        this.parent = parent;
        this.name = action == null ? null: action.name;
        this.action = action == null ? null : action.action;
        this.properties = action == null ? null : action.properties;
    }

    public boolean isItem() {
        return children.isEmpty() && action != null;
    }

    public String[] getChildPath() {
        int i = 0;
        for (MenuActionTreeElement e = parent; e != null; e = e.parent) {
            i++;
        }
        final String[] path = new String[i];
        i = path.length - 2;
        for (MenuActionTreeElement e = parent; e != null && i >= 0; e = e.parent) {
            path[i--] = e.name;
        }
        path[path.length - 1] = name;
        return path;
    }

    private void fillMenu(JMenu menu, MenuActionTreeElement element) {
        for (final Map.Entry<String, TreeMap<String, MenuActionTreeElement>> me : element.children.entrySet()) {
            if (!me.getKey().equals(element.children.firstKey())) {
                menu.addSeparator();
            }
            for (final Map.Entry<String, MenuActionTreeElement> ce : me.getValue().entrySet()) {
                if (ce.getValue().isItem()) {
                    menu.add(ce.getValue().action);
                } else {
                    final JMenu subMenu = new JMenu(s(ce.getKey()));
                    menu.add(subMenu);
                    fillMenu(subMenu, ce.getValue());
                }
            }
        }
    }

    public void fillJMenuBar(JMenuBar menuBar) {
        for (final Map.Entry<String, TreeMap<String, MenuActionTreeElement>> e : children.entrySet()) {
            if (!e.getKey().equals(children.firstKey())) {
                menuBar.add(new JSeparator(JSeparator.VERTICAL));
            }
            for (final Map.Entry<String, MenuActionTreeElement> ce : e.getValue().entrySet()) {
                final JMenu menu = new JMenu(s(ce.getKey()));
                menuBar.add(menu);
                fillMenu(menu, ce.getValue());
            }
        }
    }

    private void fillMenu(Menu menu, MenuActionTreeElement element) {
        for (final Map.Entry<String, TreeMap<String, MenuActionTreeElement>> me : element.children.entrySet()) {
            if (!me.getKey().equals(element.children.firstKey())) {
                menu.addSeparator();
            }
            for (final Map.Entry<String, MenuActionTreeElement> ce : me.getValue().entrySet()) {
                if (ce.getValue().isItem()) {
                    final MenuItem menuItem = new MenuItem(s(ce.getKey()));
                    menuItem.addActionListener(ce.getValue().action);
                    menu.add(menuItem);
                } else {
                    final Menu subMenu = new Menu(s(ce.getKey()));
                    menu.add(subMenu);
                    fillMenu(subMenu, ce.getValue());
                }
            }
        }
    }

    public void fillMenuBar(MenuBar menuBar) {
        for (final Map.Entry<String, TreeMap<String, MenuActionTreeElement>> e : children.entrySet()) {
            for (final Map.Entry<String, MenuActionTreeElement> ce : e.getValue().entrySet()) {
                final Menu menu = new Menu(s(ce.getKey()));
                menuBar.add(menu);
                fillMenu(menu, ce.getValue());
            }
        }
    }

    public void fillPopupMenu(PopupMenu popupMenu) {
        for (final Map.Entry<String, TreeMap<String, MenuActionTreeElement>> e : children.entrySet()) {
            if (!e.getKey().equals(children.firstKey())) {
                popupMenu.addSeparator();
            }
            for (final Map.Entry<String, MenuActionTreeElement> ce : e.getValue().entrySet()) {
                final Menu menu = new Menu(s(ce.getKey()));
                popupMenu.add(menu);
                fillMenu(menu, ce.getValue());
            }
        }
    }
}
