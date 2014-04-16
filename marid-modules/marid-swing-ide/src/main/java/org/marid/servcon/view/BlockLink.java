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

package org.marid.servcon.view;

import com.google.common.collect.ImmutableMap;
import org.marid.logging.LogSupport;
import org.marid.servcon.view.ga.Specie;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

/**
 * @author Dmitry Ovchinnikov.
 */
public class BlockLink<S extends Specie<S>> implements LogSupport {

    private static final int SPECIES_COUNT = 20;

    private volatile S specie;
    private final List<S> species = new ArrayList<>();
    public final BlockView.In in;
    public final BlockView.Out out;

    public BlockLink(Function<BlockLink<S>, S> specieFunc, BlockView.In in, BlockView.Out out) {
        this.in = in;
        this.out = out;
        this.specie = specieFunc.apply(this);
        for (int i = 0; i < SPECIES_COUNT; i++) {
            species.add(specieFunc.apply(this));
        }
    }

    public void paint(Graphics2D g) {
        specie.paint(g);
    }

    public void doGA() {
        try {
            final TreeMap<Double, S> specieMap = new TreeMap<>();
            final Random random = ThreadLocalRandom.current();
            while (specieMap.size() < SPECIES_COUNT * 2) {
                final S male = species.get(random.nextInt(species.size()));
                final S female = species.get(random.nextInt(species.size()));
                final S child = male.crossover(female);
                child.mutate();
                specieMap.put(child.fitness(), child);
            }
            final Map.Entry<Double, S> bestEntry = specieMap.firstEntry();
            specie = bestEntry.getValue();
            species.clear();
            for (final S specie : specieMap.values()) {
                if (species.size() > SPECIES_COUNT) {
                    break;
                }
                species.add(specie);
            }
        } catch (Exception x) {
            warning("GA error", x);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ImmutableMap.of("in", in, "out", out, "specie", specie);
    }
}
