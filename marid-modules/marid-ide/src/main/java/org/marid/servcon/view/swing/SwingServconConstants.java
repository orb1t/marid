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

package org.marid.servcon.view.swing;

import javax.swing.*;
import javax.swing.border.Border;

/**
 * @author Dmitry Ovchinnikov.
 */
public interface SwingServconConstants {

    Border EMPTY3 = BorderFactory.createEmptyBorder(3, 3, 3, 3);
    Border EMPTY5 = BorderFactory.createEmptyBorder(5, 5, 5, 5);
    Border BLOCK_BORDER = BorderFactory.createCompoundBorder(BorderFactory.createRaisedSoftBevelBorder(), EMPTY3);
    Border TITLE_BORDER = BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), EMPTY5);
}
