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

package org.marid.swing.layout;

import java.awt.*;

import static java.awt.GridBagConstraints.RELATIVE;

/**
 * @author Dmitry Ovchinnikov
 */
public interface GridBagLayoutSupport {

    default GridBagConstraints gbc(int x, int y, int w, int h, double wx, double wy, int a, int f, Insets i, int px, int py) {
        return new GridBagConstraints(x, y, w, h, wx, wy, a, f, i, px, py);
    }

    default GridBagConstraints gbc(int w, int h, double wx, double wy, int a, int f, Insets i, int px, int py) {
        return gbc(RELATIVE, RELATIVE, w, h, wx, wy, a, f, i, px, py);
    }
}
