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
import java.util.BitSet;

/**
 * @author Dmitry Ovchinnikov.
 */
public class OrthoSpecie extends Specie<OrthoSpecie> {

    private static final int BORDER = 20;
    private static final int MAX = 4;

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
        for (int i = 0; i < MAX; i++) {
            if (dirs.get(i)) {
                g.drawLine(x, y, x += lengths[i], y);
            } else {
                g.drawLine(x, y, x, y += lengths[i]);
            }
        }
        g.drawLine(x, y, x, p2.y);
        g.drawLine(x, p2.y, p2.x - BORDER, p2.y);
        g.drawLine(p2.x - BORDER, p2.y, p2.x, p2.y);
    }

    private int length(GaContext gc) {
        int len = 0;
        for (final int l : lengths) {
            len += Math.abs(l);
        }
        int x = gc.p1.x + BORDER, y = gc.p1.y;
        for (int i = 0; i < MAX; i++) {
            final int l = lengths[i];
            if (dirs.get(i)) {
                x += l;
            } else {
                y += l;
            }
        }
        return len + Math.abs(gc.p2.y - y) + Math.abs(gc.p2.x - BORDER - x);
    }

    @Override
    public double fitness(GaContext gc) {
        int isectFactor = 0;
        double distFactor = 0.0;
        for (final Rectangle r : gc.rectangles) {
            final int cx = r.x + r.width / 2;
            final int cy = r.y + r.height / 2;
            int x = gc.p1.x + BORDER, y = gc.p1.y;
            for (int i = 0; i < MAX; i++) {
                final int len = lengths[i];
                if (dirs.get(i)) {
                    if (r.intersectsLine(x, y, x + len, y)) {
                        isectFactor += r.height * 2 - Math.abs(cy - y);
                    }
                    x += len;
                } else {
                    if (r.intersectsLine(x, y, x, y + len)) {
                        isectFactor += r.width * 2 - Math.abs(cx - x);
                    }
                    distFactor += Math.sqrt(Math.abs((gc.p1.x + gc.p2.x) / 2.0 - x) * Math.abs(len));
                    y += len;
                }
            }
            if (r.intersectsLine(x, y, x, gc.p2.y)) {
                isectFactor += r.width * 2 - Math.abs(cx - x);
            }
            distFactor += Math.sqrt(Math.abs((gc.p1.x + gc.p2.x) / 2.0 - x) * Math.abs(gc.p2.y - y));
            if (r.intersectsLine(x, gc.p2.y, gc.p2.x - BORDER, gc.p2.y)) {
                isectFactor += r.height * 2 - Math.abs(cy - gc.p2.y);
            }
        }
        return length(gc) + isectFactor * 100.0 + distFactor / 10.0;
    }

    @Override
    public void mutate(GaContext gc) {
        if (gc.random.nextFloat() < gc.getMutationProbability()) {
            for (int i = 0; i < MAX; i++) {
                final int r = gc.random.nextInt(3000);
                lengths[i] += gc.random.nextInt(-r, r + 1);
                dirs.set(i, gc.random.nextBoolean());
            }
        }
    }

    @Override
    public OrthoSpecie crossover(GaContext gaContext, OrthoSpecie that) {
        final int[] lengths = new int[MAX];
        final BitSet dirs = new BitSet(MAX);
        final int rand = gaContext.random.nextInt();
        for (int i = 0; i < MAX; i++) {
            if ((rand & (1 << i)) == 0) {
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
