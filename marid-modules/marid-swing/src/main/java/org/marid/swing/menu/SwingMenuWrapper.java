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

/**
 * @author Dmitry Ovchinnikov
 */
public class SwingMenuWrapper implements SwingMenuContainer {

    protected final JMenu menu;

    public SwingMenuWrapper(JMenu menu) {
        this.menu = menu;
    }

    @Override
    public JMenu getMenu(int index) {
        int count = 0;
        for (int i = 0; i < menu.getMenuComponentCount(); i++) {
            final Component component = menu.getMenuComponent(i);
            if (component instanceof JMenu && count++ == index) {
                return (JMenu) component;
            }
        }
        return null;
    }

    @Override
    public int getMenuCount() {
        int count = 0;
        for (int i = 0; i < menu.getMenuComponentCount(); i++) {
            final Component component = menu.getMenuComponent(i);
            if (component instanceof JMenu) {
                count++;
            }
        }
        return count;
    }

    @Override
    public void add(JMenuItem menuItem) {
        menu.add(menuItem);
    }

    @Override
    public void addSeparator() {
        menu.addSeparator();
    }
}
