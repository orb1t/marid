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

package org.marid.swing.actions;

import org.marid.l10n.L10nSupport;
import org.marid.swing.menu.SwingMenuBarWrapper;
import org.marid.swing.menu.SwingMenuContainer;
import org.marid.swing.menu.SwingMenuWrapper;

import javax.swing.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridActions implements L10nSupport {

    public static final String TOOLBAR_ENABLED = "toolbarEnabled";
    public static final String MENUBAR_DISABLED = "menubarDisabled";

    public static void fillMenu(ActionMap actionMap, JMenuBar menuBar) {
        final List<Map.Entry<ActionKey, Action>> actions = Arrays.stream(actionMap.allKeys())
                .filter(k -> k instanceof ActionKey)
                .map(ActionKey.class::cast)
                .filter(k -> k.size() >= 4)
                .sorted()
                .map(k -> new AbstractMap.SimpleImmutableEntry<>(k, actionMap.get(k)))
                .filter(e -> e.getValue() != null && !Boolean.TRUE.equals(e.getValue().getValue(MENUBAR_DISABLED)))
                .collect(Collectors.toList());
        for (final ListIterator<Map.Entry<ActionKey, Action>> it = actions.listIterator(); it.hasNext(); ) {
            final Map.Entry<ActionKey, Action> e = it.next();
            final Action a = e.getValue();
            final ActionKey k = e.getKey();
            final String[] path = k.getPath();
            final JMenu menu = getOrCreateMenu(new SwingMenuBarWrapper(menuBar), path);
            if (it.hasPrevious()) {
                final Map.Entry<ActionKey, Action> pe = actions.get(it.previousIndex());
                if (Arrays.equals(pe.getKey().getPath(), path) && !pe.getKey().getGroup().equals(k.getGroup())) {
                    menu.addSeparator();
                }
            }
            if (a.getValue(Action.SELECTED_KEY) != null) {
                final JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(a);
                menuItem.setName(k.getLastName());
                menu.add(menuItem);
            } else {
                final JMenuItem menuItem = menu.add(a);
                menuItem.setName(k.getLastName());
            }
        }
    }

    public static void fillToolbar(ActionMap actionMap, JToolBar toolBar) {
        final List<Map.Entry<ActionKey, Action>> actions = Arrays.stream(actionMap.allKeys())
                .filter(k -> k instanceof ActionKey)
                .map(ActionKey.class::cast)
                .sorted()
                .map(k -> new AbstractMap.SimpleImmutableEntry<>(k, actionMap.get(k)))
                .filter(e -> e.getValue() != null && Boolean.TRUE.equals(e.getValue().getValue(TOOLBAR_ENABLED)))
                .collect(Collectors.toList());
        for (final ListIterator<Map.Entry<ActionKey, Action>> it = actions.listIterator(); it.hasNext(); ) {
            final Map.Entry<ActionKey, Action> e = it.next();
            final Action a = e.getValue();
            final ActionKey k = e.getKey();
            if (it.hasPrevious()) {
                final Map.Entry<ActionKey, Action> pe = actions.get(it.previousIndex());
                if (!pe.getKey().getGroup().equals(k.getGroup())) {
                    toolBar.addSeparator();
                }
            }
            if (a.getValue(Action.SELECTED_KEY) != null) {
                final JToggleButton toggleButton = new JToggleButton(a);
                toggleButton.setName(k.getLastName());
                toggleButton.setFocusable(false);
                toolBar.add(toggleButton);
            } else {
                final JButton button = toolBar.add(a);
                button.setName(k.getLastName());
                button.setFocusable(false);
            }
        }
    }

    public static void fillInputMap(InputMap inputMap, ActionMap actionMap) {
        for (final Object key : actionMap.allKeys()) {
            final Action a = actionMap.get(key);
            final Object keyStrokeObject = a.getValue(Action.ACCELERATOR_KEY);
            if (keyStrokeObject instanceof KeyStroke) {
                final KeyStroke keyStroke = (KeyStroke) keyStrokeObject;
                inputMap.put(keyStroke, key);
            }
        }
    }

    private static JMenu getOrCreateMenu(SwingMenuContainer wrapper, String[] path) {
        JMenu topMenu = null;
        for (int i = 0; i < wrapper.getMenuCount(); i++) {
            final JMenu menu = wrapper.getMenu(i);
            if (path[0].equals(menu.getName())) {
                topMenu = menu;
                break;
            }
        }
        if (topMenu == null) {
            topMenu = new JMenu(LS.s(path[0]));
            topMenu.setName(path[0]);
            wrapper.add(topMenu);
        }
        if (path.length == 1) {
            return topMenu;
        } else {
            return getOrCreateMenu(new SwingMenuWrapper(topMenu), Arrays.copyOf(path, path.length - 1));
        }
    }
}
