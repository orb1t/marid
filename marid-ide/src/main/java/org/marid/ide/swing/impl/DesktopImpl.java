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

package org.marid.ide.swing.impl;

import org.marid.ide.itf.Desktop;

import javax.swing.*;

/**
 * Desktop implementation.
 *
 * @author Dmitry Ovchinnikov 
 */
public class DesktopImpl extends JDesktopPane implements Desktop {

    private static final long serialVersionUID = 6775788688564987554L;

    public DesktopImpl() {
        setDesktopManager(new DesktopManagerImpl());
    }

    private class DesktopManagerImpl extends DefaultDesktopManager {

        private static final long serialVersionUID = 6164252027191486209L;

        @Override
        public void openFrame(JInternalFrame f) {
            super.openFrame(f);
            if (f.getClass().getSimpleName().contains("Dialog")) {
                int x = (getWidth() - f.getWidth()) / 2;
                int y = (getHeight() - f.getHeight()) / 2;
                if (x < 0) {
                    x = 0;
                }
                if (y < 0) {
                    y = 0;
                }
                f.setLocation(x, y);
            }
        }
    }
}
