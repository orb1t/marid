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
import java.awt.geom.Path2D;

import static org.marid.bd.shapes.LinkShapeType.OrthoLinkConfigurationEditor.join;

/**
 * @author Dmitry Ovchinnikov
 */
public class OrthoLinkShape extends LinkShape {

    protected Path2D.Float path;

    public OrthoLinkShape(BlockComponent.Output output, BlockComponent.Input input) {
        super(output, input);
        update();
    }

    @Override
    public void update() {
        path = new Path2D.Float();
        final Point out = output.getConnectionPoint();
        final Point in = input.getConnectionPoint();
        final int cx = (out.x + in.x) / 2;
        final int sx = Integer.compare(out.x, in.x);
        final int sy = Integer.compare(out.y, in.y);
        path.moveTo(out.x, out.y);
        path.lineTo(cx + sx * join, out.y);
        path.lineTo(cx, out.y - sy * join);
        path.lineTo(cx, in.y + sy * join);
        path.lineTo(cx - sx * join, in.y);
        path.lineTo(in.x, in.y);
    }

    @Override
    public void paint(Graphics2D g) {
        g.draw(path);
    }

    @Override
    public Shape getShape() {
        return path;
    }
}
