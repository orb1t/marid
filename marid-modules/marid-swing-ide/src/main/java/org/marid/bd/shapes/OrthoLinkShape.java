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
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
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

    public Path2D.Float getPath(int dogLeg, Point out, Point in) {
        final Path2D.Float path = new Path2D.Float();
        path.moveTo(out.x, out.y);
        path.lineTo(dogLeg, out.y);
        path.lineTo(dogLeg, in.y);
        path.lineTo(in.x, in.y);
        return path;
    }

    @Override
    public void update() {
        final Point out = output.getConnectionPoint(), in = input.getConnectionPoint();
        path = getPath(getDogLeg(out, in), out, in);
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
    public void paint(Graphics2D g) {
        super.paint(g);
        final List<Line2D.Float> lines = lines(path);
        output.getBlockComponent().getSchemaEditor().getLinkShapes().stream()
                .filter(l -> l.output.getOutput() == output.getOutput() && l != this)
                .filter(l -> l instanceof OrthoLinkShape)
                .map(OrthoLinkShape.class::cast)
                .forEach(s -> lines(s.path).forEach(l1 -> lines.forEach(l2 -> intersection(g, l1, l2))));
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
                final Path2D path = getPath(cx + dx, out, in);
                final List<Line2D.Float> lines = lines(path);
                if (lines.stream().allMatch(l -> rectangles.stream().noneMatch(r -> r.intersectsLine(l)))) {
                    return cx + dx;
                }
            }
            {
                final Path2D path = getPath(cx - dx, out, in);
                final List<Line2D.Float> lines = lines(path);
                if (lines.stream().allMatch(l -> rectangles.stream().noneMatch(r -> r.intersectsLine(l)))) {
                    return cx - dx;
                }
            }
        }
        return cx;
    }

    private static List<Line2D.Float> lines(Path2D path) {
        final List<Line2D.Float> lines = new ArrayList<>();
        final float[] coords = new float[2], current = new float[2];
        for (final PathIterator it = path.getPathIterator(null); !it.isDone(); it.next()) {
            switch (it.currentSegment(current)) {
                case PathIterator.SEG_LINETO:
                    lines.add(new Line2D.Float(coords[0], coords[1], current[0], current[1]));
                case PathIterator.SEG_MOVETO:
                    System.arraycopy(current, 0, coords, 0, coords.length);
                    break;
            }
        }
        return lines;
    }

    private static void intersection(Graphics2D g, Line2D.Float l1, Line2D.Float l2) {
        if (l1.x1 == l1.x2 && l2.y1 == l2.y2 && l1.ptSegDist(l2.getP1()) < 0.001) {
            g.fill(new Ellipse2D.Float(l1.x1 - 2.0f, l2.y1 - 2.0f, 5.0f, 5.0f));
        } else if (l1.x1 == l1.x2 && l2.x1 == l2.x2 && l1.y1 == l2.y1 && (l2.y2 - l2.y1) * (l1.y2 - l1.y1) < 0.0) {
            g.fill(new Ellipse2D.Float(l1.x1 - 2.0f, l2.y1 - 2.0f, 5.0f, 5.0f));
        }
    }
}
