/*
 * Copyright (C) 2015 Dmitry Ovchinnikov
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

/**
 * @author Dmitry Ovchinnikov
 */
public class SwingPopupMenuWrapper implements SwingMenuContainer {

    private final JPopupMenu popupMenu;

    public SwingPopupMenuWrapper(JPopupMenu popupMenu) {
        this.popupMenu = popupMenu;
    }

    @Override
    public JMenu getMenu(int index) {
        for (int i = 0, j = 0; i < popupMenu.getComponentCount(); i++) {
            if (popupMenu.getComponent(i) instanceof JMenu) {
                if (j++ == index) {
                    return (JMenu) popupMenu.getComponent(i);
                }
            }
        }
        return null;
    }

    @Override
    public int getMenuCount() {
        int count = 0;
        for (int i = 0; i < popupMenu.getComponentCount(); i++) {
            if (popupMenu.getComponent(i) instanceof JMenu) {
                count++;
            }
        }
        return count;
    }

    @Override
    public void add(JMenuItem menuItem) {
        popupMenu.add(menuItem);
    }

    @Override
    public void addSeparator() {
        popupMenu.addSeparator();
    }
}
