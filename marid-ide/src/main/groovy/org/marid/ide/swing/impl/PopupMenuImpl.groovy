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
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.util.List

/**
 * Popup menu implementation.
 *
 * @author Dmitry Ovchinnikov 
 */
class PopupMenuImpl extends PopupMenu implements ActionListener {

    private final ApplicationImpl application;
    private final font = UIManager.getFont("Label.font").deriveFont(Font.PLAIN);

    PopupMenuImpl(ApplicationImpl app, List<MenuEntry> menuEntries) {
        application = app;
        def activateItem = new MenuItem("Show/Hide the main window".ls());
        activateItem.font = font.deriveFont(Font.BOLD);
        activateItem.actionCommand = "show_hide";
        activateItem.addActionListener(this);
        add(activateItem);
        addSeparator();
        def showLogItem = new MenuItem("Show log".ls());
        showLogItem.font = font;
        showLogItem.actionCommand = "show_log";
        showLogItem.addActionListener(this);
        add(showLogItem);
        addSeparator();
        def items = menuEntries.findAll { it.path.length == 0 };
        for (def entry in items.sort(false, { e1, e2 -> e1.priority <=> e2.priority })) {
            switch (entry.type) {
                case MenuType.ITEM:
                    add(getItem(entry));
                    break;
                case MenuType.CHECKED:
                    add(getCheckedItem(entry));
                    break;
                case MenuType.RADIO:
                    add(getItem(entry));
                    break;
                case MenuType.MENU:
                    add(getMenu(menuEntries, entry));
                    break;
            }
        }
    }

    private def getItem(MenuEntry entry) {
        def action = new MenuActionImpl(entry);
        def item = new MenuItem(action.getValue(Action.NAME) as String);
        item.font = font;
        item.addActionListener(action);
        item.actionCommand = action.getValue(Action.ACTION_COMMAND_KEY);
        return item;
    }

    private def getCheckedItem(MenuEntry entry) {
        def action = new MenuActionImpl(entry);
        def item = new CheckboxMenuItem(action.getValue(Action.NAME) as String);
        item.font = font;
        item.addActionListener(action);
        item.actionCommand = action.getValue(Action.ACTION_COMMAND_KEY);
        item.state = action.getValue(Action.SELECTED_KEY) as boolean;
        return item;
    }

    private def getMenu(List<MenuEntry> menuEntries, MenuEntry entry) {
        def action = new MenuActionImpl(entry);
        def Menu menu = new Menu(action.getValue(Action.NAME) as String);
        menu.font = font;
        menu.addActionListener(action);
        def path = entry.path;
        def n = path.length;
        def v = menuEntries.findAll { it.path.length == n + 1 && it.path == entry.path + entry.name };
        for (def e in v.sort(false, { e1, e2 -> e1.priority <=> e2.priority })) {
            switch (e.type) {
                case MenuType.ITEM:
                    menu.add(getItem(e));
                    break;
                case MenuType.CHECKED:
                    menu.add(getCheckedItem(e));
                    break;
                case MenuType.RADIO:
                    menu.add(getItem(e));
                    break;
                case MenuType.MENU:
                    menu.add(getMenu(menuEntries, e));
                    break;
            }
        }
        return menu;
    }

    @Override
    void show(Component origin, int x, int y) {
        update(this);
        super.show(origin, x, y);
    }

    @Override
    void actionPerformed(ActionEvent e) {
        switch (e.actionCommand) {
            case "show_hide":
                application.frame.visible = !application.frame.visible;
                break;
            case "show_log":
                application.showLog();
                break;
        }
    }

    private void update(Menu menu) {
        for (def i = 0; i < menu.itemCount; i++) {
            def item = menu.getItem(i);
            def action = (MenuActionImpl) item.actionListeners.find { it instanceof MenuActionImpl };
            if (action != null) {
                action.update();
                item.enabled = action.enabled;
                if (item instanceof CheckboxMenuItem) {
                    def cbi = (CheckboxMenuItem) item;
                    cbi.state = action.getValue(Action.SELECTED_KEY) as boolean;
                }
            }
            if (item instanceof Menu) {
                update((Menu) item);
            }
        }
    }
}
