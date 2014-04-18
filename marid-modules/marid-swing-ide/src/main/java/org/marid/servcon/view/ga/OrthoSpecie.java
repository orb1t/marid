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
    private static final int COUNT = 8;

    private final int[] lengths;
    private final BitSet dirs;

    public OrthoSpecie(BlockLink<OrthoSpecie> blockLink) {
        super(blockLink);
        this.lengths = new int[COUNT];
        this.dirs = new BitSet(COUNT);
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
        for (int i = 0; i < COUNT; i++) {
            final int len = lengths[i];
            if (dirs.get(i)) {
                g.drawLine(x, y, x += len, y);
            } else {
                g.drawLine(x, y, x, y += len);
            }
        }
        g.drawLine(x, y, p2.x - BORDER, p2.y);
        g.drawLine(p2.x - BORDER, p2.y, p2.x, p2.y);
    }

    @Override
    public double fitness(GaContext gaContext) {
        final double dist = gaContext.p1.distance(gaContext.p2) + 1.0;
        final double distFactor = Stats.intSum(lengths, 0, COUNT) / dist;
        return 0;
    }

    @Override
    public void mutate(GaContext gaContext) {
        if (gaContext.random.nextFloat() < MUTATION_PROBABILITY) {
            for (int i = 0; i < COUNT; i++) {
                final int r = gaContext.random.nextInt(300);
                lengths[i] += gaContext.random.nextInt(r * 2 + 1) - r;
                dirs.set(i, gaContext.random.nextBoolean());
            }
        }
    }

    @Override
    public OrthoSpecie crossover(GaContext gaContext, OrthoSpecie that) {
        final int[] lengths = new int[COUNT];
        final BitSet dirs = new BitSet(COUNT);
        for (int i = 0; i < COUNT; i++) {
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
