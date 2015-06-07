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

package org.marid.swing.geom;

import java.awt.*;

/**
 * @author Dmitry Ovchinnikov
 */
public interface Dimensions {

    static Dimension atLeast(int minWidth, int minHeight, Dimension dimension) {
        if (dimension.width >= minWidth && dimension.height >= minHeight) {
            return dimension;
        } else {
            return new Dimension(Math.max(minWidth, dimension.width), Math.max(minHeight, dimension.height));
        }
    }

    static Rectangle atLeast(int minWidth, int minHeight, Rectangle r) {
        if (r.width >= minWidth && r.height >= minHeight) {
            return r;
        } else {
            return new Rectangle(r.x, r.y, Math.max(minWidth, r.width), Math.max(minHeight, r.height));
        }
    }
}
