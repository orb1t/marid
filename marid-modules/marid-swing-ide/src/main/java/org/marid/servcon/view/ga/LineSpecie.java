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

    private final int n = 3;
    private final Point[] points;

    public LineSpecie(BlockLink<LineSpecie> blockLink) {
        super(blockLink);
        points = new Point[n];
        for (int i = 0; i < n; i++) {
            points[i] = blockLink.in.connectionPoint();
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
            double isectFactor = 0.0;
            /*
            for (final Component component : blockLink.in.getEditor().getComponents()) {
                final Rectangle bounds = component.getBounds();
                for (int i = 0; i < points.length - 1; i++) {
                    if (bounds.intersectsLine(new Line2D.Double(points[i], points[i + 1]))) {
                        isectFactor++;
                    }
                }
            }*/
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
                points[i] = new Point(p.x + random.nextInt(5) - 2, p.y + random.nextInt(5) - 2);
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
