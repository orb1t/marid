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

package org.marid.bd.common;

import org.marid.bd.Block;
import org.marid.swing.dnd.DndSource;

import java.awt.datatransfer.DataFlavor;

/**
 * @author Dmitry Ovchinnikov
 */
public interface DndBlockSource extends DndSource<Block> {

    DataFlavor[] BLOCK_DATA_FLAVORS = {new DataFlavor(Block.class, null)};

    @Override
    default int getDndActions() {
        return DND_COPY;
    }

    @Override
    default DataFlavor[] getSourceDataFlavors() {
        return BLOCK_DATA_FLAVORS;
    }
}
