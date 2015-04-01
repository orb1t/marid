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

package org.marid.bd;

import java.awt.*;

/**
 * @author Dmitry Ovchinnikov
 */
public interface BlockColors {

    int ANNOTATIONS_BLOCK_COLOR = 0x206090;
    int EXPRESSIONS_BLOCK_COLOR = 0x0000FF;
    int META_BLOCK_COLOR = 0x10AAAA;
    int MULTIPLEXORS_BLOCK_COLOR = 0x404040;
    int STATEMENTS_BLOCK_COLOR = 0x005500;

    int BLACK = 0x000000;
    int BLUE = 0x0000FF;
    int RED = 0xFF0000;

    static Color getBlockColor(String id) {
        try {
            return new Color((int) BlockColors.class.getField(id.toUpperCase() + "_BLOCK_COLOR").get(null));
        } catch (Exception x) {
            return Color.BLACK;
        }
    }
}
