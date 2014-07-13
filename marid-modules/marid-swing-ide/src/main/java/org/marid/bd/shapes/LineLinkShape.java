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
import java.awt.geom.Line2D;

/**
 * @author Dmitry Ovchinnikov
 */
public class LineLinkShape extends LinkShape {

    public LineLinkShape(BlockComponent.Output output, BlockComponent.Input input) {
        super(output, input);
    }

    @Override
    public void update() {

    }

    @Override
    public void paint(Graphics2D g) {
        final Point p1 = output.getConnectionPoint();
        final Point p2 = input.getConnectionPoint();
        g.drawLine(p1.x, p1.y, p2.x, p2.y);
    }

    @Override
    public Shape getShape() {
        return new Line2D.Double(output.getConnectionPoint(), input.getConnectionPoint());
    }
}
