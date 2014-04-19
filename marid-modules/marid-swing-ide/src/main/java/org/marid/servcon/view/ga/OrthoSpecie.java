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
import org.marid.collections.Stats;
import org.marid.servcon.view.BlockLink;

import java.awt.*;
import java.util.BitSet;

/**
 * @author Dmitry Ovchinnikov.
 */
public class OrthoSpecie extends Specie<OrthoSpecie> {

    private static final int BORDER = 20;
    private static final int MAX = 8;

    private final int[] lengths;
    private final BitSet dirs;

    public OrthoSpecie(BlockLink<OrthoSpecie> blockLink) {
        super(blockLink);
        this.lengths = new int[MAX];
        this.dirs = new BitSet(MAX);
    }

    private OrthoSpecie(BlockLink<OrthoSpecie> blockLink, int[] lengths, BitSet dirs) {
        super(blockLink);
        this.lengths = lengths;
        this.dirs = dirs;
    }

    @Override
    public void paint(Graphics2D g) {
        final Point p1 = blockLink.out.connectionPoint();
        final Point p2 = blockLink.in.connectionPoint();
        int x = p1.x + BORDER, y = p1.y;
        g.drawLine(p1.x, p1.y, x, y);
        final int m = MAX / 2;
        int xm1 = 0, ym1 = 0, xm2 = 0, ym2 = 0;
        for (int i = 0; i < MAX; i++) {
            if (i == m) {
                xm1 = x;
                ym1 = y;
                x = p2.x - BORDER;
                y = p2.y;
                g.drawLine(x, y, p2.x, p2.y);
            }
            if (dirs.get(i)) {
                g.drawLine(x, y, x += lengths[i], y);
            } else {
                g.drawLine(x, y, x, y += lengths[i]);
            }
            if (i == m) {
                xm2 = x;
                ym2 = y;
            }
        }
        g.drawLine(xm1, ym1, xm1, ym2);
        g.drawLine(xm1, ym2, xm2, ym2);
    }

    private int intersectionH(Rectangle r, int x1, int x2) {
        if (x1 >= r.x && x2 <= r.x + r.width) {
            return x2 - x1;
        } else if (x1 < r.x && x2 > r.x + r.width) {
            return r.width;
        } else if (x1 >= r.x && x1 < r.x + r.width && x2 > r.x + r.width) {
            return r.x + r.width - x1;
        } else if (x1 < r.x && x2 > r.x && x2 <= r.x + r.width) {
            return x2 - r.x;
        } else {
            return 0;
        }
    }

    private int intersectionV(Rectangle r, int y1, int y2) {
        if (y1 >= r.y && y2 <= r.y + r.height) {
            return y2 - y1;
        } else if (y1 < r.y && y2 > r.y + r.height) {
            return r.height;
        } else if (y1 >= r.y && y1 < r.y + r.height && y2 > r.y + r.height) {
            return r.y + r.height - y1;
        } else if (y1 < r.y && y2 > r.y && y2 <= r.y + r.height) {
            return y2 - r.y;
        } else {
            return 0;
        }
    }

    @Override
    public double fitness(GaContext gaContext) {
        final int distFactor = Stats.intAbsSum(lengths, 0, MAX);
        int isectFactor = 0;
        int x = gaContext.p1.x + BORDER, y = gaContext.p1.y;
        final int m = MAX / 2;
        int xm1 = 0, ym1 = 0;
        double endDist = 0.0;
        for (int i = 0; i < MAX; i++) {
            if (i == m) {
                xm1 = x;
                ym1 = y;
                x = gaContext.p2.x - BORDER;
                y = gaContext.p2.y;
            }
            final int len = lengths[i];
            if (dirs.get(i)) {
                for (final Rectangle r : gaContext.rectangles) {
                    if (y >= r.y && y <= r.y + r.height) {
                        isectFactor += intersectionH(r, len > 0 ? x : x + len, len > 0 ? x + len : x);
                    }
                }
                x += len;
            } else {
                for (final Rectangle r : gaContext.rectangles) {
                    if (x >= r.x && x <= r.x + r.width) {
                        isectFactor += intersectionV(r, len > 0 ? y : y + len, len > 0 ? y + len : y);
                    }
                }
                y += len;
            }
            if (i == m) {
                endDist = Point.distance(xm1, ym1, x, y);
                for (final Rectangle r : gaContext.rectangles) {
                    if (r.intersectsLine(xm1, ym1, x, y)) {
                        isectFactor += endDist;
                    }
                }
            }
        }
        return distFactor + isectFactor * 5.0 + endDist;
    }

    @Override
    public void mutate(GaContext gc) {
        if (gc.random.nextFloat() < gc.getMutationProbability()) {
            for (int i = 0; i < MAX; i++) {
                final int r = gc.random.nextInt(300);
                lengths[i] += gc.random.nextInt(r * 2 + 1) - r;
                dirs.set(i, gc.random.nextBoolean());
            }
        }
    }

    @Override
    public OrthoSpecie crossover(GaContext gaContext, OrthoSpecie that) {
        final int[] lengths = new int[MAX];
        final BitSet dirs = new BitSet(MAX);
        for (int i = 0; i < MAX; i++) {
            if (gaContext.random.nextBoolean()) {
                lengths[i] = that.lengths[i];
                dirs.set(i, that.dirs.get(i));
            } else {
                lengths[i] = this.lengths[i];
                dirs.set(i, this.dirs.get(i));
            }
        }
        return new OrthoSpecie(blockLink, lengths, dirs);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ImmutableMap.of("lengths", lengths, "dirs", dirs.toString());
    }
}
