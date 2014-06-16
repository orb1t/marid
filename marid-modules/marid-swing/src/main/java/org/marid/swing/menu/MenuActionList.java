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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Dmitry Ovchinnikov
 */
public class MenuActionList extends ArrayList<MenuAction> {

    public MenuActionTreeElement createTreeElement() {
        final MenuActionTreeElement root = new MenuActionTreeElement(null, null);
        stream().filter(a -> a.path.length == 0).forEach(a -> {
            final MenuActionTreeElement element = new MenuActionTreeElement(root, a);
            final TreeMap<String, MenuActionTreeElement> map = root.children.computeIfAbsent(a.group, g -> new TreeMap<>());
            map.put(a.name, element);
            fillMenuActionTreeElement(element);
        });
        return root;
    }

    public void fillToolbar(JToolBar toolBar) {
        final AtomicBoolean first = new AtomicBoolean(true);
        final AtomicReference<String> oldGroup = new AtomicReference<>();
        final AtomicReference<String[]> oldPath = new AtomicReference<>();
        stream().filter(a -> Boolean.TRUE.equals(a.properties.get("toolbar"))).sorted((a1, a2) -> {
            final int min = Math.min(a1.path.length, a2.path.length);
            for (int i = 0; i < min; i++) {
                final int c = a1.path[i].compareTo(a2.path[i]);
                if (c != 0) {
                    return c;
                }
            }
            final int nc = Integer.compare(a1.path.length, a2.path.length);
            if (nc != 0) {
                return nc;
            }
            final int gc = a1.group.compareTo(a2.group);
            return gc != 0 ? gc : a1.name.compareTo(a2.name);
        }).forEach(a -> {
            if (!Objects.equals(oldGroup.get(), a.group) || !Arrays.equals(oldPath.get(), a.path)) {
                if (!first.compareAndSet(true, false)) {
                    toolBar.addSeparator();
                }
            }
            oldGroup.set(a.group);
            oldPath.set(a.path);
            toolBar.add(a.action).setFocusable(false);
        });
    }

    public ActionBuilder add(boolean toolbar, String group, String name, String... path) {
        final ActionBuilder actionBuilder = new ActionBuilder(name);
        final MenuAction menuAction = new MenuAction(name, group, path, actionBuilder);
        if (toolbar) {
            menuAction.properties.put("toolbar", true);
        }
        add(menuAction);
        return actionBuilder;
    }

    public ActionBuilder add(String group, String name, String... path) {
        return add(false, group, name, path);
    }

    private void fillMenuActionTreeElement(MenuActionTreeElement element) {
        stream().filter(a -> Arrays.equals(a.path, element.getChildPath())).forEach(a -> {
            final TreeMap<String, MenuActionTreeElement> map = element.children.computeIfAbsent(a.group, g -> new TreeMap<>());
            final MenuActionTreeElement child = new MenuActionTreeElement(element, a);
            map.put(a.name, child);
            fillMenuActionTreeElement(child);
        });
    }
}
