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
import org.marid.swing.menu.SwingMenuContainer;
import org.marid.swing.menu.SwingMenuWrapper;

import javax.swing.*;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.lang.Boolean.TRUE;
import static java.util.stream.IntStream.range;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridActions implements L10nSupport {

    public static final String TOOLBAR_ENABLED = "toolbarEnabled";
    public static final String MENUBAR_DISABLED = "menubarDisabled";

    private static List<Entry<ActionKey, Action>> actions(JComponent comp, Predicate<Entry<ActionKey, Action>> filter) {
        final ActionMap actionMap = comp.getActionMap();
        final Object[] keys = actionMap.allKeys() == null ? new Object[0] : actionMap.allKeys();
        return Arrays.stream(keys)
                .filter(k -> k instanceof ActionKey)
                .map(ActionKey.class::cast)
                .sorted()
                .map(k -> new SimpleImmutableEntry<>(k, actionMap.get(k)))
                .filter(e -> e.getValue() != null)
                .filter(filter)
                .collect(Collectors.toList());
    }

    public static void fillMenu(JComponent pane, SwingMenuContainer swingMenuContainer) {
        final List<Entry<ActionKey, Action>> actions = actions(pane,
                e -> e.getKey().size() >= 4 && !TRUE.equals(e.getValue().getValue(MENUBAR_DISABLED)));
        for (final ListIterator<Entry<ActionKey, Action>> it = actions.listIterator(); it.hasNext(); ) {
            if (it.hasPrevious() && it.hasNext()) {
                final Entry<ActionKey, Action> ne = actions.get(it.nextIndex());
                final Entry<ActionKey, Action> pe = actions.get(it.previousIndex());
                final ActionKey nk = ne.getKey(), pk = pe.getKey();
                final JMenu menu = getOrCreateMenu(swingMenuContainer, nk.getPath());
                if (Arrays.equals(pk.getPath(), nk.getPath()) && !pk.getGroup().equals(nk.getGroup())) {
                    menu.addSeparator();
                }
            }
            final Entry<ActionKey, Action> e = it.next();
            final Action a = e.getValue();
            final ActionKey k = e.getKey();
            final String[] path = k.getPath();
            final JMenu menu = getOrCreateMenu(swingMenuContainer, path);
            if (a.getValue(Action.SELECTED_KEY) != null) {
                final JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(a);
                menuItem.setName(k.getLastName());
                menu.add(menuItem);
            } else {
                final JMenuItem menuItem = menu.add(a);
                menuItem.setName(k.getLastName());
            }
        }
        fillInputMap(pane, actions);
    }

    public static void fillToolbar(JComponent comp, JToolBar toolBar) {
        final List<Entry<ActionKey, Action>> actions = actions(comp,
                e -> TRUE.equals(e.getValue().getValue(TOOLBAR_ENABLED)));
        for (final ListIterator<Entry<ActionKey, Action>> it = actions.listIterator(); it.hasNext(); ) {
            if (it.hasPrevious() && it.hasNext()) {
                final Entry<ActionKey, Action> ne = actions.get(it.nextIndex());
                final Entry<ActionKey, Action> pe = actions.get(it.previousIndex());
                if (!pe.getKey().getGroup().equals(ne.getKey().getGroup())) {
                    toolBar.addSeparator();
                }
            }
            final Entry<ActionKey, Action> e = it.next();
            final Action a = e.getValue();
            final ActionKey k = e.getKey();
            if (a.getValue(Action.SELECTED_KEY) != null) {
                final JToggleButton toggleButton = new JToggleButton(a);
                toggleButton.setName(k.getLastName());
                toggleButton.setFocusable(false);
                toggleButton.setHideActionText(true);
                toolBar.add(toggleButton);
            } else {
                final JButton button = toolBar.add(a);
                button.setName(k.getLastName());
                button.setFocusable(false);
            }
        }
        fillInputMap(comp, actions);
    }

    private static void fillInputMap(JComponent pane, List<Entry<ActionKey, Action>> actions) {
        actions.forEach(e -> {
            final Object keyStrokeObject = e.getValue().getValue(Action.ACCELERATOR_KEY);
            if (keyStrokeObject instanceof KeyStroke) {
                final KeyStroke keyStroke = (KeyStroke) keyStrokeObject;
                pane.getInputMap().put(keyStroke, e.getKey());
            }
        });
    }

    private static JMenu getOrCreateMenu(SwingMenuContainer wrapper, String[] path) {
        final JMenu topMenu = range(0, wrapper.getMenuCount()).mapToObj(wrapper::getMenu)
                .filter(i -> i != null && path[0].equals(i.getName())).findFirst().orElseGet(() -> {
                    final JMenu m = new JMenu(LS.s(path[0]));
                    m.setName(path[0]);
                    wrapper.add(m);
                    return m;
                });
        final int n = path.length;
        return n == 1 ? topMenu : getOrCreateMenu(new SwingMenuWrapper(topMenu), Arrays.copyOf(path, n - 1));
    }
}
