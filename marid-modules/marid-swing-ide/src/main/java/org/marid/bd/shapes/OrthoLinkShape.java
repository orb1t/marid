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
import org.marid.bd.schema.SchemaEditor;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.List;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

/**
 * @author Dmitry Ovchinnikov
 */
public class OrthoLinkShape extends LinkShape {

    protected Path2D.Float path;

    public OrthoLinkShape(BlockComponent.Output output, BlockComponent.Input input) {
        super(output, input);
        update();
    }

    public Line2D.Float[] getLines(int dogLeg, Point out, Point in) {
        return new Line2D.Float[] {
                new Line2D.Float(out.x, out.y, dogLeg, out.y),
                new Line2D.Float(dogLeg, out.y, dogLeg, in.y),
                new Line2D.Float(dogLeg, in.y, in.x, in.y)
        };
    }

    @Override
    public void update() {
        final Point out = output.getConnectionPoint(), in = input.getConnectionPoint();
        final Line2D.Float[] lines = getLines(getDogLeg(out, in), out, in);
        final Path2D.Float path = new Path2D.Float();
        path.moveTo(lines[0].x1, lines[0].y1);
        for (final Line2D.Float line : lines) {
            path.lineTo(line.x2, line.y2);
        }
        this.path = path;
    }

    private int getDogLeg(Point out, Point in) {
        final int cx = (out.x + in.x) / 2;
        final int limit = Math.abs(in.x - out.x) / 2;
        final SchemaEditor editor = output.getBlockComponent().getSchemaEditor();
        final List<Rectangle> rs;
        synchronized (editor.getTreeLock()) {
            rs = stream(editor.getComponents()).map(Component::getBounds).collect(toList());
        }
        return dogLeg(cx, limit, rs, out, in);
    }

    @Override
    public Shape getShape() {
        return path;
    }

    private int dogLeg(int cx, int limit, List<Rectangle> rectangles, Point out, Point in) {
        out = new Point(out.x + 1, out.y);
        in = new Point(in.x - 1, in.y);
        for (int dx = 0; dx < limit; dx += 5) {
            {
                final Line2D.Float[] lines = getLines(cx + dx, out, in);
                if (stream(lines).allMatch(l -> rectangles.stream().noneMatch(r -> r.intersectsLine(l)))) {
                    return cx + dx;
                }
            }
            {
                final Line2D.Float[] lines = getLines(cx - dx, out, in);
                if (stream(lines).allMatch(l -> rectangles.stream().noneMatch(r -> r.intersectsLine(l)))) {
                    return cx - dx;
                }
            }
        }
        return cx;
    }
}
