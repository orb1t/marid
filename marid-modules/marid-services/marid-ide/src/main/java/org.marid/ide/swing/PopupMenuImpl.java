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

package org.marid.ide.swing;

import org.marid.ide.menu.MenuEntry;
import org.marid.l10n.Localized;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import static org.marid.util.CollectionUtils.concat;

/**
 * @author Dmitry Ovchinnikov
 */
public class PopupMenuImpl extends PopupMenu implements ActionListener, Localized {

    private final Font font = UIManager.getFont("Label.font").deriveFont(Font.PLAIN);

    public PopupMenuImpl(List<MenuEntry> menuEntries) {
        final MenuItem activateItem = new MenuItem(S.l("Show/Hide the main window"));
        activateItem.setFont(font.deriveFont(Font.BOLD));
        activateItem.setActionCommand("show_hide");
        activateItem.addActionListener(this);
        add(activateItem);
        addSeparator();
        final MenuItem showLogItem = new MenuItem(S.l("Show log"));
        showLogItem.setFont(font);
        showLogItem.setActionCommand("show_log");
        showLogItem.addActionListener(this);
        add(showLogItem);
        addSeparator();
        final MenuItem exitItem = new MenuItem(S.l("Exit"));
        exitItem.setFont(font);
        exitItem.setActionCommand("exit");
        exitItem.addActionListener(this);
        add(exitItem);
        addSeparator();
        final TreeSet<MenuEntry> set = new TreeSet<>(MenuEntry.MENU_ENTRY_COMPARATOR);
        for (final MenuEntry e : menuEntries) {
            if (e.getPath().length == 0) {
                set.add(e);
            }
        }
        for (final MenuEntry e : set) {
            switch (e.getType()) {
                case ITEM:
                case RADIO:
                    add(getItem(e));
                    break;
                case CHECKED:
                    add(getCheckedItem(e));
                    break;
                case MENU:
                    add(getMenu(menuEntries, e));
                    break;
            }
        }
    }

    private MenuItem getItem(MenuEntry entry) {
        final MenuActionImpl action = new MenuActionImpl(entry);
        final MenuItem item = new MenuItem(String.valueOf(action.getValue(Action.NAME)));
        item.setFont(font);
        item.addActionListener(action);
        item.setActionCommand(String.valueOf(action.getValue(Action.ACTION_COMMAND_KEY)));
        return item;
    }

    private CheckboxMenuItem getCheckedItem(MenuEntry entry) {
        final MenuActionImpl action = new MenuActionImpl(entry);
        final CheckboxMenuItem item = new CheckboxMenuItem(String.valueOf(action.getValue(Action.NAME)));
        item.setFont(font);
        item.addActionListener(action);
        item.setActionCommand(String.valueOf(action.getValue(Action.ACTION_COMMAND_KEY)));
        item.setState(Boolean.TRUE.equals(action.getValue(Action.SELECTED_KEY)));
        return item;
    }

    private Menu getMenu(List<MenuEntry> menuEntries, MenuEntry entry) {
        final MenuActionImpl action = new MenuActionImpl(entry);
        final Menu menu = new Menu(String.valueOf(action.getValue(Action.NAME)));
        menu.setFont(font);
        menu.addActionListener(action);
        final String[] path = entry.getPath();
        final int n = path.length;
        final TreeSet<MenuEntry> set = new TreeSet<>(MenuEntry.MENU_ENTRY_COMPARATOR);
        for (final MenuEntry e : menuEntries) {
            if (e.getPath().length == n + 1 && Arrays.equals(e.getPath(),
                    concat(entry.getPath(), entry.getName()))) {
                set.add(e);
            }
        }
        for (final MenuEntry e : set) {
            switch (e.getType()) {
                case ITEM:
                case RADIO:
                    menu.add(getItem(e));
                    break;
                case CHECKED:
                    menu.add(getCheckedItem(e));
                    break;
                case MENU:
                    menu.add(getMenu(menuEntries, e));
                    break;
            }
        }
        return menu;
    }

    @Override
    public void show(Component origin, int x, int y) {
        update(this);
        super.show(origin, x, y);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "show_hide":
                SwingIde.frame.setVisible(!SwingIde.frame.isVisible());
                break;
            case "show_log":
                SwingIde.frame.showLog();
                break;
            case "exit":
                SwingIde.frame.exitWithConfirm();
                break;
        }

    }

    private void update(Menu menu) {
        for (int i = 0; i < menu.getItemCount(); i++) {
            final MenuItem item = menu.getItem(i);
            for (final ActionListener al : item.getActionListeners()) {
                if (al instanceof MenuActionImpl) {
                    ((MenuActionImpl) al).update();
                    item.setEnabled(((MenuActionImpl) al).isEnabled());
                    if (item instanceof CheckboxMenuItem) {
                        final Action a = (Action) al;
                        final boolean s = Boolean.TRUE.equals(a.getValue(Action.SELECTED_KEY));
                        ((CheckboxMenuItem) item).setState(s);
                    }
                }
            }
            if (item instanceof Menu) {
                update((Menu) item);
            }
        }
    }
}
