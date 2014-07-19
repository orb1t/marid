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

package org.marid.bd.shapes;

import org.marid.bd.BlockComponent;

import java.awt.*;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class LinkShape {

    public final BlockComponent.Output output;
    public final BlockComponent.Input input;

    public LinkShape(BlockComponent.Output output, BlockComponent.Input input) {
        this.output = output;
        this.input = input;
    }

    public abstract void update();

    public abstract void paint(Graphics2D g);

    public abstract Shape getShape();

    @Override
    public int hashCode() {
        return output.hashCode() ^ input.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LinkShape) {
            final LinkShape that = (LinkShape) obj;
            return this.input == that.input && this.output == that.output;
        } else {
            return false;
        }
    }
}
