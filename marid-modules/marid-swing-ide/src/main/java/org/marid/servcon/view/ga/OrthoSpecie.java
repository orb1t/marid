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

import com.google.common.collect.ImmutableMap;
import org.marid.servcon.view.BlockLink;

import java.awt.*;
import java.awt.geom.Path2D;
import java.util.Arrays;

/**
 * @author Dmitry Ovchinnikov.
 */
public class OrthoSpecie extends Specie<OrthoSpecie> {

    private static final int BORDER = 20;
    private static final int MAX = 3;

    private final int[] xs;
    private final int[] ys;
    private Path2D.Double shape;

    public OrthoSpecie(BlockLink<OrthoSpecie> blockLink) {
        super(blockLink);
        this.xs = new int[MAX];
        this.ys = new int[MAX];
    }

    private OrthoSpecie(BlockLink<OrthoSpecie> blockLink, int[] xs, int[] ys) {
        super(blockLink);
        this.xs = xs;
        this.ys = ys;
    }

    private Path2D.Double createShape() {
        final Path2D.Double shape = new Path2D.Double();
        final Point p1 = blockLink.out.connectionPoint();
        final Point p2 = blockLink.in.connectionPoint();
        shape.moveTo(p1.x, p1.y);
        int x = p1.x + BORDER, y = p1.y;
        shape.lineTo(x, y);
        for (int i = 0; i < MAX; i++) {
            shape.lineTo(x = xs[i], y);
            shape.lineTo(x, y = ys[i]);
        }
        shape.lineTo(x, p2.y);
        shape.lineTo(p2.x - BORDER, p2.y);
        shape.lineTo(p2.x, p2.y);
        return shape;
    }

    @Override
    public void paint(Graphics2D g) {
        g.draw(shape = createShape());
    }

    @Override
    public Shape getShape() {
        return shape == null ? shape = createShape() : shape;
    }

    private int length(GaContext gc) {
        int len = 0;
        int x = gc.p1.x + BORDER, y = gc.p1.y;
        for (int i = 0; i < MAX; i++) {
            len += Math.abs(xs[i] - x) + Math.abs(ys[i] - y);
            x = xs[i]; y = ys[i];
        }
        return len + Math.abs(gc.p2.y - y) + Math.abs(gc.p2.x - BORDER - x);
    }

    @Override
    public double fitness(GaContext gc) {
        int isectFactor = 0, distFactor = 0;
        for (final Rectangle r : gc.rectangles) {
            final int cx = r.x + r.width / 2;
            final int cy = r.y + r.height / 2;
            int x = gc.p1.x + BORDER, y = gc.p1.y;
            for (int i = 0; i < MAX; i++) {
                if (r.intersectsLine(x, y, x = xs[i], y)) {
                    isectFactor += r.height * 2 - Math.abs(cy - y);
                }
                final int ly = Math.abs(ys[i] - y);
                if (r.intersectsLine(x, y, x, y = ys[i])) {
                    isectFactor += r.width * 2 - Math.abs(cx - x);
                }
                distFactor += Math.abs((gc.p1.x + gc.p2.x) / 2 - x) + ly;
            }
            if (r.intersectsLine(x, y, x, gc.p2.y)) {
                isectFactor += r.width * 2 - Math.abs(cx - x);
            }
            distFactor += Math.abs((gc.p1.x + gc.p2.x) / 2 - x) + Math.abs(gc.p2.y - y);
            if (r.intersectsLine(x, gc.p2.y, gc.p2.x - BORDER, gc.p2.y)) {
                isectFactor += r.height * 2 - Math.abs(cy - gc.p2.y);
            }
        }
        return length(gc) + Math.pow(isectFactor, 4.0) * 10.0 + Math.sqrt(distFactor);
    }

    @Override
    public void mutate(GaContext gc) {
        if (gc.random.nextFloat() < gc.getMutationProbability()) {
            for (int i = 0; i < MAX; i++) {
                final int r = gc.random.nextInt(3000);
                xs[i] += gc.random.nextInt(-r, r + 1);
                ys[i] += gc.random.nextInt(-r, r + 1);
            }
        }
    }

    @Override
    public OrthoSpecie crossover(GaContext gaContext, OrthoSpecie that) {
        final int[] xs = new int[MAX];
        final int[] ys = new int[MAX];
        final int rand = gaContext.random.nextInt();
        for (int i = 0; i < MAX; i++) {
            if ((rand & (1 << i)) == 0) {
                xs[i] = that.xs[i];
                ys[i] = that.ys[i];
            } else {
                xs[i] = this.xs[i];
                ys[i] = this.ys[i];
            }
        }
        return new OrthoSpecie(blockLink, xs, ys);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ImmutableMap.of("xs", Arrays.toString(xs), "ys", Arrays.toString(ys));
    }
}
