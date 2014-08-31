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

import org.marid.l10n.L10nSupport;
import org.marid.util.CollectionUtils;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.TreeSet;
import java.util.function.Consumer;

/**
 * @author Dmitry Ovchinnikov
 */
public class ActionTreeElement implements Comparable<ActionTreeElement>, L10nSupport {

    private final ActionTreeElement parent;
    private final String name;
    private final String group;
    private final Action action;
    private final TreeSet<ActionTreeElement> children = new TreeSet<>();

    protected ActionTreeElement(ActionTreeElement parent, String name, String group, Action action) {
        this.parent = parent;
        this.name = Objects.requireNonNull(name, "Name must be non-null");
        this.group = group;
        this.action = action;
    }

    public ActionTreeElement() {
        this(null, "", null, null);
    }

    public ActionTreeElement(MenuActionList menuActions) {
        this();
        fill(new String[0], menuActions);
    }

    public ActionTreeElement add(String name, String group, Consumer<ActionTreeElement> filler) {
        final ActionTreeElement element = new ActionTreeElement(this, name, group, null);
        children.add(element);
        filler.accept(element);
        return this;
    }

    public ActionTreeElement add(String name, String group, Action action) {
        final ActionTreeElement element = new ActionTreeElement(this, name, group, action);
        children.add(element);
        return this;
    }

    private void fill(String[] path, MenuActionList menuActions) {
        menuActions.stream().filter(a -> a.path != null && Arrays.equals(path, a.path)).forEach(a -> {
            final ActionTreeElement e = new ActionTreeElement(this, a.name, a.group, a.action);
            e.fill(CollectionUtils.concat(path, a.name), menuActions);
            children.add(e);
        });
    }

    public boolean isItem() {
        return children.isEmpty();
    }

    public String[] getChildPath() {
        final ArrayList<String> paths = new ArrayList<>();
        for (ActionTreeElement e = this; e != null; e = e.parent) {
            paths.add(0, e.name);
        }
        return paths.toArray(new String[paths.size()]);
    }

    public void fillMenu(JMenu menu) {
        for (final ActionTreeElement e : children) {
            final ActionTreeElement prev = children.lower(e);
            if (prev != null && !Objects.equals(e.group, prev.group)) {
                menu.addSeparator();
            }
            if (e.isItem()) {
                if (e.action != null) {
                    final JMenuItem menuItem = menu.add(e.action);
                    menuItem.setName(e.name);
                }
            } else {
                final JMenu sub = new JMenu(s(e.name));
                sub.setName(e.name);
                e.fillMenu(sub);
                menu.add(sub);
            }
        }
    }

    public void fillJMenuBar(JMenuBar menuBar) {
        for (final ActionTreeElement e : children) {
            if (!e.isItem()) {
                final JMenu menu = new JMenu(s(e.name));
                menu.setName(e.name);
                e.fillMenu(menu);
                menuBar.add(menu);
            }
        }
    }

    public void fillMenu(Menu menu) {
        for (final ActionTreeElement e : children) {
            final ActionTreeElement prev = children.lower(e);
            if (prev != null && !Objects.equals(e.group, prev.group)) {
                menu.addSeparator();
            }
            if (e.isItem()) {
                if (e.action != null) {
                    final MenuItem menuItem = menu.add(new MenuItem(e.action.getValue(Action.NAME).toString()));
                    menuItem.setName(e.name);
                }
            } else {
                final Menu sub = new Menu(s(e.name));
                sub.setName(e.name);
                e.fillMenu(sub);
                menu.add(sub);
            }
        }
    }

    public void fillMenuBar(MenuBar menuBar) {
        for (final ActionTreeElement e : children) {
            if (!e.isItem()) {
                final Menu menu = new Menu(s(e.name));
                menu.setName(e.name);
                e.fillMenu(menu);
                menuBar.add(menu);
            }
        }
    }

    @Override
    public int compareTo(@Nonnull ActionTreeElement o) {
        final int gc = (group == null ? "" : group).compareTo(o.group == null ? "" : o.group);
        return gc != 0 ? gc : name.compareTo(o.name);
    }
}
