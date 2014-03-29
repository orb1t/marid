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

package org.marid.swing.dnd;

import javax.swing.*;
import java.io.Serializable;

import static java.awt.Image.SCALE_SMOOTH;

/**
 * @author Dmitry Ovchinnikov.
 */
public interface DndObject extends Serializable {

    Object getObject();

    default ImageIcon getVisualRepresentation() {
        return null;
    }

    default ImageIcon getVisualRepresentation(int width, int height) {
        final ImageIcon icon = getVisualRepresentation();
        if (icon == null) {
            return null;
        } else {
            return icon.getIconWidth() == width && icon.getIconHeight() == height
                    ? icon
                    : new ImageIcon(icon.getImage().getScaledInstance(width, height, SCALE_SMOOTH));
        }
    }
}
