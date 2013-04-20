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

package org.marid.ide.menu;

import java.awt.event.ActionEvent;
import java.util.Comparator;

public interface MenuEntry {

    public static final Comparator<MenuEntry> MENU_ENTRY_COMPARATOR = new Comparator<MenuEntry>() {

        @Override
        public int compare(MenuEntry o1, MenuEntry o2) {
            int c = Integer.compare(o1.getPath().length, o2.getPath().length);
            if (c != 0) {
                return c;
            } else {
                String[] p1 = o1.getPath();
                String[] p2 = o2.getPath();
                for (int i = 0; i < p1.length; i++) {
                    c = p1[i].compareTo(p2[i]);
                    if (c != 0) {
                        return c;
                    }
                }
                c = Integer.compare(o1.getPriority(), o2.getPriority());
                if (c != 0) {
                    return c;
                }
                return o1.getName().compareTo(o2.getName());
            }
        }
    };

    public String[] getPath();

    public String getGroup();

    public String getLabel();

    public String getName();

    public String getCommand();

    public String getShortcut();

    public String getDescription();

    public String getInfo();

    public String getIcon();

    public MenuType getType();

    public int getPriority();

    public boolean isMutableIcon();

    public boolean isMutableLabel();

    public boolean isMutableInfo();

    public boolean isMutableDescription();

    public boolean hasSelectedPredicate();

    public boolean hasEnabledPredicate();

    public Boolean isSelected();

    public boolean isEnabled();

    public boolean isLeaf();

    public void call(ActionEvent event);
}