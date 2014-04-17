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

package org.marid.servcon.view.ga;

import com.google.common.util.concurrent.AtomicDouble;
import org.marid.logging.LogSupport;
import org.marid.servcon.view.BlockLink;
import org.marid.swing.geom.LineCoordinateVisitor;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.Arrays;
import java.util.Random;

/**
 * @author Dmitry Ovchinnikov.
 */
public class LineSpecie extends Specie<LineSpecie> implements LogSupport {

    private static final int BORDER = 20;
    private static final int COUNT = 4;

    private final int[] xs;
    private final int[] ys;

    public LineSpecie(BlockLink<LineSpecie> blockLink) {
        super(blockLink);
        final Random random = new Random();
        this.xs = new int[COUNT];
        this.ys = new int[COUNT];
        for (int i = 0; i < COUNT; i++) {
            xs[i] = random.nextInt(100);
            ys[i] = random.nextInt(100);
        }
    }

    private LineSpecie(BlockLink<LineSpecie> blockLink, int[] xs, int[] ys) {
        super(blockLink);
        this.xs = xs;
        this.ys = ys;
    }

    private double length(GaContext fc) {
        double length = Point.distance(fc.p1.x + BORDER, fc.p1.y, xs[0], ys[0]);
        for (int i = 0; i < COUNT - 1; i++) {
            length += Point.distance(xs[i], ys[i], xs[i + 1], ys[i + 1]);
        }
        length += Point.distance(xs[COUNT - 1], ys[COUNT - 1], fc.p2.x - BORDER, fc.p2.y);
        return length;
    }

    @Override
    public void paint(Graphics2D g) {
        final Point p1 = blockLink.out.connectionPoint();
        final Point p2 = blockLink.in.connectionPoint();
        g.drawLine(p1.x, p1.y, p1.x + BORDER, p1.y);
        g.drawLine(p1.x + BORDER, p1.y, xs[0], ys[0]);
        g.drawPolyline(xs, ys, COUNT);
        g.drawLine(xs[COUNT - 1], ys[COUNT - 1], p2.x - BORDER, p2.y);
        g.drawLine(p2.x - BORDER, p2.y, p2.x, p2.y);
    }

    @Override
    public double fitness(GaContext fc) {
        try {
            final double lineDistance = Point.distance(fc.p1.x + BORDER, fc.p1.y, fc.p2.x - BORDER, fc.p2.y);
            final double distance = length(fc);
            final double distFactor = lineDistance == 0.0 ? (distance == 0.0 ? 0.0 : 1.0) : distance / lineDistance;
            final AtomicDouble isectFactor = new AtomicDouble();
            for (final Rectangle r : fc.rectangles) {
                final double cx = r.getCenterX();
                final double cy = r.getCenterY();
                final double rr = Point.distance(cx, cy, r.getMinY(), r.getMinY());
                visitLines(fc, (x1, y1, x2, y2) -> {
                    if (r.intersectsLine(x1, y1, x2, y2)) {
                        isectFactor.addAndGet(rr - Line2D.ptLineDist(x1, y1, x2, y2, cx, cy));
                    }
                });
            }
            return distFactor + isectFactor.get() / (lineDistance * 0.25);
        } catch (Exception x) {
            warning("GA fitness error on {0}", x, this);
            return 0.0;
        }
    }

    @Override
    public void mutate(GaContext gc) {
        if (gc.random.nextFloat() < MUTATION_PROBABILITY) {
            for (int i = 0; i < COUNT; i++) {
                final int rx = gc.random.nextInt(300);
                final int ry = gc.random.nextInt(300);
                xs[i] += gc.random.nextInt(rx * 2 + 1) - rx;
                ys[i] += gc.random.nextInt(ry * 2 + 1) - ry;
            }
        }
    }

    @Override
    public LineSpecie crossover(GaContext gc, LineSpecie that) {
        final int[] xs = new int[COUNT], ys = new int[COUNT];
        for (int i = 0; i < COUNT; i++) {
            if (gc.random.nextBoolean()) {
                xs[i] = that.xs[i];
                ys[i] = that.ys[i];
            } else {
                xs[i] = this.xs[i];
                ys[i] = this.ys[i];
            }
        }
        return new LineSpecie(blockLink, xs, ys);
    }

    private void visitLines(GaContext fc, LineCoordinateVisitor visitor) {
        visitor.visit(fc.p1.x + BORDER, fc.p1.y, xs[0], ys[0]);
        for (int i = 0; i < COUNT - 1; i++) {
            visitor.visit(xs[i], ys[i], xs[i + 1], ys[i + 1]);
        }
        visitor.visit(xs[COUNT - 1], ys[COUNT - 1], fc.p2.x - BORDER, fc.p2.y);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + Arrays.toString(xs) + Arrays.toString(ys);
    }
}
