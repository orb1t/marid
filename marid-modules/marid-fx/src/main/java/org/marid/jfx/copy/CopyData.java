/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
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

package org.marid.jfx.copy;

import javafx.scene.input.TransferMode;

/**
 * @author Dmitry Ovchinnikov
 */
public class CopyData<N, E> {

    public final TransferMode transferMode;
    public final TransferMode[] transferModes;
    public final N node;
    public final E element;

    public CopyData(TransferMode transferMode, N node, E element, TransferMode... transferModes) {
        this.transferMode = transferMode;
        this.node = node;
        this.element = element;
        this.transferModes = transferModes;
    }
}
