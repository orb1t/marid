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
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static java.lang.Math.abs;
import static java.lang.Math.min;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

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
        final Point out = output.getConnectionPoint(), in = input.getConnectionPoint();
        path = getPath(getDogLeg(out, in), out, in);
    }

    private int getDogLeg(Point out, Point in) {
        final int cx = (out.x + in.x) / 2;
        final SchemaEditor editor = output.getBlockComponent().getSchemaEditor();
        final List<Rectangle> rs;
        synchronized (editor.getTreeLock()) {
            rs = stream(editor.getComponents()).map(Component::getBounds).collect(toList());
        }
        final Set<List<Line2D.Float>> links = editor.getLinkShapes().stream()
                .filter(l -> l.output.getOutput() != output.getOutput())
                .filter(l -> l.input.getInput() != input.getInput())
                .filter(l -> l != this && l instanceof OrthoLinkShape)
                .map(OrthoLinkShape.class::cast)
                .map(s -> lines(s.path))
                .collect(toSet());
        return dogLeg(cx, rs, links, out, in);
    }

    @Override
    public void paint(Graphics2D g) {
        super.paint(g);
        final List<Line2D.Float> lines = lines(path);
        output.getBlockComponent().getSchemaEditor().getLinkShapes().stream()
                .filter(l -> l != this && l.output.getOutput() == output.getOutput() && l instanceof OrthoLinkShape)
                .map(OrthoLinkShape.class::cast)
                .flatMap(s -> lines(s.path).stream().map(l -> intersection(l, lines)))
                .filter(p -> p != null)
                .forEach(p -> paintIntersection(g, p));
        output.getBlockComponent().getSchemaEditor().getLinkShapes().stream()
                .filter(l -> l != this && l.input.getInput() == input.getInput() && l instanceof OrthoLinkShape)
                .map(OrthoLinkShape.class::cast)
                .flatMap(s -> lines(s.path).stream().map(l -> intersection(l, lines)))
                .filter(p -> p != null)
                .forEach(p -> paintIntersection(g, p));
    }

    @Override
    public Shape getShape() {
        return path;
    }

    private int dogLeg(int cx, List<Rectangle> rectangles, Set<List<Line2D.Float>> links, Point out, Point in) {
        out = new Point(out.x + 1, out.y);
        in = new Point(in.x - 1, in.y);
        int dl = cx, fitness = Integer.MAX_VALUE;
        final int x0 = min(out.x, in.x), xf = Math.max(out.x, in.x);
        for (int x = x0; x <= xf; x += 5) {
            final int f = fitness(x, cx, rectangles, links, out, in);
            if (f < fitness) {
                fitness = f;
                dl = x;
            }
        }
        return dl;
    }

    static Path2D.Float getPath(int dogLeg, Point out, Point in) {
        final Path2D.Float path = new Path2D.Float();
        path.moveTo(out.x, out.y);
        path.lineTo(dogLeg, out.y);
        path.lineTo(dogLeg, in.y);
        path.lineTo(in.x, in.y);
        return path;
    }

    static List<Line2D.Float> lines(Path2D path) {
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

    static void paintIntersection(Graphics2D g, Point2D.Float p) {
        g.fill(new Ellipse2D.Float(p.x - 2.0f, p.y - 2.0f, 5.0f, 5.0f));
    }

    static Point2D.Float intersectionPoint(Line2D.Float l1, Line2D.Float l2) {
        final float x1 = l1.x1, y1 = l1.y1, x2 = l1.x2, y2 = l1.y2, x3 = l2.x1, y3 = l2.y1, x4 = l2.x2, y4 = l2.y2;
        final float d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        if (d == 0) {
            return null;
        }
        final float x = ((x2 - x1) * (x3 * y4 - x4 * y3) - (x4 - x3) * (x1 * y2 - x2 * y1)) / d;
        final float y = ((y3 - y4) * (x1 * y2 - x2 * y1) - (y1 - y2) * (x3 * y4 - x4 * y3)) / d;
        return l1.ptSegDist(x, y) > 0.001 || l2.ptSegDist(x, y) > 0.001 ? null : new Point2D.Float(x, y);
    }

    static Point2D.Float intersection(Line2D.Float l1, List<Line2D.Float> lines) {
        for (final Line2D.Float line : lines) {
            final Point2D.Float p = intersectionPoint(line, l1);
            if (p != null) {
                final BitSet set = new BitSet(4);
                int c = IntStream.range(0, 4).map(dir -> seg(l1, p.x, p.y, set, dir)).sum();
                for (final Line2D.Float l2 : lines) {
                    c += IntStream.range(0, 4).map(dir -> seg(l2, p.x, p.y, set, dir)).sum();
                    if (c > 2) {
                        return p;
                    }
                }
            }
        }
        return null;
    }

    static int seg(Line2D.Float line, float x, float y, BitSet set, int direction) {
        if (set.get(direction)) {
            return 0;
        } else {
            switch (direction) {
                case 0:
                    x--;
                    break;
                case 1:
                    x++;
                    break;
                case 2:
                    y--;
                    break;
                case 3:
                    y++;
                    break;
                default:
                    throw new IllegalArgumentException(Integer.toString(direction));
            }
            if (line.ptSegDist(x, y) < 0.001) {
                set.set(direction);
                return 1;
            } else {
                return 0;
            }
        }
    }

    static int fitness(int x, int cx, List<Rectangle> rectangles, Set<List<Line2D.Float>> links, Point out, Point in) {
        int fitness = abs(x - cx);
        final int len = abs(in.x - out.x);
        final Path2D.Float path = getPath(x, out, in);
        final List<Line2D.Float> lines = lines(path);
        fitness += rectangles.stream()
                .mapToInt(r -> lines.stream()
                        .filter(r::intersectsLine)
                        .mapToInt(l -> len)
                        .sum())
                .sum();
        fitness += lines.stream()
                .mapToInt(l1 -> links.stream()
                        .mapToInt(l -> l.stream()
                                .filter(l2 -> isParallel(l1, l2))
                                .mapToInt(l2 -> len)
                                .sum())
                        .sum())
                .sum();
        return fitness;
    }

    static boolean isParallel(Line2D.Float l1, Line2D.Float l2) {
        return !(l1.x1 != l1.x2 || l2.x1 != l2.x2)
                && Math.abs(l2.x1 - l1.x1) <= 5.0f
                && Line2D.linesIntersect(0.0, l1.y1, 0.0, l1.y2, 0.0, l2.y1, 0.0, l2.y2);
    }
}
