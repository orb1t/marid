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

import org.marid.servcon.view.BlockLink;

import java.awt.*;

/**
* @author Dmitry Ovchinnikov.
*/
public abstract class Specie<S extends Specie<S>> {

    public static final float MUTATION_PROBABILITY = 0.005f;

    protected final BlockLink<S> blockLink;

    public Specie(BlockLink<S> blockLink) {
        this.blockLink = blockLink;
    }

    public abstract void paint(Graphics2D g);

    public abstract double fitness(GaContext gaContext);

    public abstract void mutate(GaContext gaContext);

    public abstract S crossover(GaContext gaContext, S that);
}