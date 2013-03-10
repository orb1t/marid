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

package org.marid.ide.util

import org.marid.image.MaridIcon

import java.awt.*
import java.awt.image.BufferedImage
import java.util.List

/**
 * Marid icons.
 *
 * @author Dmitry Ovchinnikov 
 */
class MaridIcons {

    private static final List<BufferedImage> icons = Arrays.asList(
        MaridIcon.getImage(16, Color.GREEN),
        MaridIcon.getImage(22, Color.GREEN),
        MaridIcon.getImage(24, Color.GREEN),
        MaridIcon.getImage(32, Color.GREEN)
    );

    static List<BufferedImage> getIcons() {
        return icons;
    }
}
