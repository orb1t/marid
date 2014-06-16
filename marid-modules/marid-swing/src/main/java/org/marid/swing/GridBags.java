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

package org.marid.swing;

import java.awt.*;

import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.RELATIVE;
import static org.marid.swing.SwingFinals.DEFAULT_INSETS;

/**
 * @author Dmitry Ovchinnikov
 */
public class GridBags {

    public static GridBagConstraints constraints(int gw, int gh, double wx, double wy, int anchor, int fill) {
        return new GridBagConstraints(RELATIVE, RELATIVE, gw, gh, wx, wy, anchor, fill, DEFAULT_INSETS, 0, 0);
    }

    public static GridBagConstraints constraints(int gw, int gh, double wx, double wy, int anchor) {
        return constraints(gw, gh, wx, wy, anchor, BOTH);
    }
}
