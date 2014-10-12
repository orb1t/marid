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
import java.awt.geom.Path2D;
import java.util.concurrent.ThreadLocalRandom;

import static org.marid.bd.shapes.LinkShapeType.LiveLinkConfigurationEditor.mutationProbability;
import static org.marid.bd.shapes.LiveLinkShape.LiveLinkShapeData.COUNT;

/**
 * @author Dmitry Ovchinnikov
 */
public class LiveLinkShape extends AbstractLiveLinkShape<LiveLinkShape.LiveLinkShapeData> {

    public LiveLinkShape(BlockComponent.Output output, BlockComponent.Input input) {
        super(output, input);
    }

    @Override
    protected LiveLinkShapeData defaultSpecie() {
        return new LiveLinkShapeData(output.getConnectionPoint(), input.getConnectionPoint());
    }

    @Override
    protected LiveLinkShapeData crossover(LiveLinkShapeData male, LiveLinkShapeData female, ThreadLocalRandom random) {
        final int[] xs = new int[COUNT], ys = new int[COUNT];
        final int rand = random.nextInt();
        for (int i = 0; i < COUNT; i++) {
            if ((rand & (1 << i)) == 0) {
                xs[i] = male.xs[i];
                ys[i] = male.ys[i];
            } else {
                xs[i] = female.xs[i];
                ys[i] = female.ys[i];
            }
        }
        return new LiveLinkShapeData(xs, ys);
    }

    @Override
    protected void mutate(LiveLinkShapeData specie, ThreadLocalRandom random) {
        if (random.nextDouble() < mutationProbability) {
            final int r = random.nextInt(5000);
            for (int i = 0; i < COUNT; i++) {
                specie.xs[i] += random.nextInt(-r, r + 1);
                specie.ys[i] += random.nextInt(-r, r + 1);
            }
        }
    }

    private double lengthSq(LiveLinkShapeData data) {
        double length = Point.distanceSq(out.x, out.y, data.xs[0], data.ys[0]);
        for (int i = 0; i < COUNT - 1; i++) {
            length += Point.distanceSq(data.xs[i], data.ys[i], data.xs[i + 1], data.ys[i + 1]);
        }
        length += Point.distanceSq(data.xs[COUNT - 1], data.ys[COUNT - 1], in.x, in.y);
        return length;
    }

    private double isectF(Rectangle r, double cx, double cy, double rr, double x1, double y1, double x2, double y2) {
        if (r.intersectsLine(x1, y1, x2, y2)) {
            return rr - Line2D.ptLineDistSq(x1, y1, x2, y2, cx, cy);
        } else {
            return 0.0;
        }
    }

    @Override
    protected double fitness(LiveLinkShapeData specie) {
        final Point out = this.out, in = this.in;
        try {
            final double lineDistance = Point.distanceSq(out.x, out.y, in.x, in.y) + 0.1;
            final double distFactor = lengthSq(specie) / lineDistance;
            double isectFactor = 0.0;
            for (final Rectangle r : rectangles) {
                final double cx = r.getCenterX();
                final double cy = r.getCenterY();
                final double rr = r.width * r.width + r.height * r.height;
                isectFactor += isectF(r, cx, cy, rr, out.x + 1, out.y, specie.xs[0], specie.ys[0]);
                for (int i = 0; i < COUNT - 1; i++) {
                    isectFactor += isectF(r, cx, cy, rr, specie.xs[i], specie.ys[i], specie.xs[i + 1], specie.ys[i + 1]);
                }
                isectFactor += isectF(r, cx, cy, rr, specie.xs[COUNT - 1], specie.ys[COUNT - 1], in.x - 1, in.y);
            }
            return distFactor + isectFactor;
        } catch (Exception x) {
            warning("GA fitness error on {0}", x, this);
            return 0.0;
        }
    }

    @Override
    public void paint(Graphics2D g) {
        g.draw(getShape());
    }

    @Override
    public Shape getShape() {
        final Point p1 = output.getConnectionPoint();
        final Point p2 = input.getConnectionPoint();
        final Path2D.Double shape = new Path2D.Double();
        shape.moveTo(p1.x, p1.y);
        final LiveLinkShapeData data = bestSpecie;
        for (int i = 0; i < COUNT; i++) {
            shape.lineTo(data.xs[i], data.ys[i]);
        }
        shape.lineTo(p2.x, p2.y);
        return shape;
    }

    protected static class LiveLinkShapeData {

        protected static final int COUNT = 5;

        protected final int[] xs;
        protected final int[] ys;

        protected LiveLinkShapeData(int[] xs, int[] ys) {
            this.xs = xs;
            this.ys = ys;
        }

        public LiveLinkShapeData() {
            this(new int[COUNT], new int[COUNT]);
        }

        public LiveLinkShapeData(Point p1, Point p2) {
            this();
            final int dx = (p2.x - p1.x) / COUNT;
            final int dy = (p2.y - p1.y) / COUNT;
            for (int i = 0; i < COUNT; i++) {
                xs[i] = p1.x + dx * (i + 1);
                ys[i] = p1.y + dy * (i + 1);
            }
        }
    }
}
