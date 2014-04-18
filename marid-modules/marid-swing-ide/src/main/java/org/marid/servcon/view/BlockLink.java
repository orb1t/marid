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
import org.marid.servcon.view.ga.GaContext;
import org.marid.servcon.view.ga.Specie;

import java.awt.*;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.IntFunction;

/**
 * @author Dmitry Ovchinnikov.
 */
public class BlockLink<S extends Specie<S>> implements LogSupport {

    private static final int SPECIES_COUNT = 30;

    private volatile S specie;
    private final S[] species;
    private final Species<S> incubator;
    public final BlockView.In in;
    public final BlockView.Out out;

    public BlockLink(Function<BlockLink<S>, S> specieFunc, IntFunction<S[]> saf, BlockView.In in, BlockView.Out out) {
        this.in = in;
        this.out = out;
        this.specie = specieFunc.apply(this);
        this.species = saf.apply(SPECIES_COUNT);
        for (int i = 0; i < SPECIES_COUNT; i++) {
            this.species[i] = specieFunc.apply(this);
        }
        this.incubator = new Species<>(saf, SPECIES_COUNT * 4);
    }

    public void paint(Graphics2D g) {
        specie.paint(g);
    }

    public void doGA(GaContext gaContext) {
        incubator.count = 0;
        try {
            while (incubator.count < incubator.fitnesses.length) {
                final S male = species[gaContext.random.nextInt(species.length)];
                final S female = species[gaContext.random.nextInt(species.length)];
                final S child = male.crossover(gaContext, female);
                child.mutate(gaContext);
                incubator.put(child.fitness(gaContext), child);
            }
            specie = incubator.species[0];
            incubator.copy(SPECIES_COUNT, species);
        } catch (Exception x) {
            warning("GA error", x);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ImmutableMap.of("in", in, "out", out, "specie", specie);
    }

    static class Species<S extends Specie<S>> {

        final double[] fitnesses;
        final S[] species;
        int count;

        Species(IntFunction<S[]> func, int count) {
            fitnesses = new double[count];
            species = func.apply(count);
        }

        void put(double fitness, S specie) {
            final int index = -(Arrays.binarySearch(fitnesses, 0, count, fitness) + 1);
            if (index < 0) {
                return;
            }
            System.arraycopy(fitnesses, index, fitnesses, index + 1, count - index);
            System.arraycopy(species, index, species, index + 1, count - index);
            fitnesses[index] = fitness;
            species[index] = specie;
            count++;
        }

        void copy(int count, S[] species) {
            System.arraycopy(this.species, 0, species, 0, count);
        }
    }
}
