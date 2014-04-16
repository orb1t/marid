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

import org.marid.logging.LogSupport;
import org.marid.servcon.view.BlockLink;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
* @author Dmitry Ovchinnikov.
*/
public class LineSpecie extends Specie<LineSpecie> implements LogSupport {

    private static final int BORDER = 20;
    private final int n = 3;
    private final Point[] points;

    public LineSpecie(BlockLink<LineSpecie> blockLink) {
        super(blockLink);
        points = new Point[n];
        final Random random = new Random();
        final Point p1 = blockLink.out.connectionPoint();
        final Point p2 = blockLink.in.connectionPoint();
        final Point cp = new Point((p1.x + p2.x) / 2, (p1.y + p2.y) / 2);
        for (int i = 0; i < n; i++) {
            points[i] = new Point(cp.x + random.nextInt(10), cp.y + random.nextInt(10));
        }
    }

    public LineSpecie(BlockLink<LineSpecie> blockLink, Point[] points) {
        super(blockLink);
        this.points = points;
    }

    private Point[] points() {
        final Point[] points = new Point[n + 2];
        System.arraycopy(this.points, 0, points, 1, n);
        points[0] = blockLink.out.connectionPoint();
        points[points.length - 1] = blockLink.in.connectionPoint();
        return points;
    }

    @Override
    public void paint(Graphics2D g) {
        final Point[] points = points();
        for (int i = 0; i < n + 1; i++) {
            g.draw(new Line2D.Double(points[i], points[i + 1]));
        }
    }


    @Override
    public double fitness() {
        final Point[] points = points();
        try {
            final double lineDistance = points[0].distance(points[points.length - 1]);
            double distance = 0.0;
            for (int i = 0; i < points.length - 1; i++) {
                distance += points[i].distance(points[i + 1]);
            }
            final double distFactor = lineDistance == 0.0 ? (distance == 0.0 ? 0.0 : 1.0) : distance / lineDistance;
            final Rectangle[] rectangles;
            synchronized (blockLink.in.getEditor().getTreeLock()) {
                rectangles = new Rectangle[blockLink.in.getEditor().getComponentCount()];
                for (int i = 0; i < rectangles.length; i++) {
                    rectangles[i] = blockLink.in.getEditor().getComponent(i).getBounds();
                }
            }
            double isectFactor = 0.0;
            for (final Rectangle r : rectangles) {
                final int x = r.x - BORDER;
                final int y = r.y - BORDER;
                final int w = r.width + BORDER * 2;
                final int h = r.height + BORDER * 2;
                final Rectangle b = new Rectangle(x, y, w, h);
                for (int i = 0; i < points.length - 1; i++) {
                    final Rectangle rect = i ==0 || i == points.length - 2 ? r : b;
                    final Line2D line = new Line2D.Double(points[i], points[i + 1]);
                    if (rect.intersectsLine(line)) {
                        final Dimension d = rect.intersection(line.getBounds()).getSize();
                        isectFactor += Math.sqrt(d.width * d.width + d.height * d.height) + 1.0;
                    }
                }
            }
            return distFactor + isectFactor;
        } catch (Exception x) {
            warning("GA fitness error on {0}", x, Arrays.toString(points));
            return 0.0;
        }
    }

    @Override
    public void mutate() {
        final Random random = ThreadLocalRandom.current();
        for (int i = 0; i < n; i++) {
            if (random.nextFloat() < MUTATION_PROBABILITY) {
                final Point p = points[i];
                final int rx = random.nextInt(30);
                final int ry = random.nextInt(30);
                points[i] = new Point(p.x + random.nextInt(rx * 2 + 1) - rx, p.y + random.nextInt(ry * 2 + 1) - ry);
            }
        }
    }

    @Override
    public LineSpecie crossover(LineSpecie that) {
        final Random random = ThreadLocalRandom.current();
        final Point[] points = new Point[n];
        for (int i = 0; i < n; i++) {
            if (random.nextBoolean()) {
                points[i] = that.points[i];
            } else {
                points[i] = this.points[i];
            }
        }
        return new LineSpecie(blockLink, points);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + Arrays.toString(points);
    }
}
