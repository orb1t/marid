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

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.marid.servcon.view.ga.GaContext;
import org.marid.servcon.view.ga.Specie;
import org.marid.test.NormalTests;

import java.awt.*;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import static java.util.Arrays.stream;
import static org.junit.Assert.assertArrayEquals;

/**
 * @author Dmitry Ovchinnikov.
 */
@Category({NormalTests.class})
public class BlockLinkTest {

    @Test
    public void testAddElements() {
        final BlockLink<TestSpecie> link = new BlockLink<>(30, l -> new TestSpecie(), TestSpecie[]::new, null, null);
        link.initIncubator(4);
        for (int iter = 0; iter < 10; iter++) {
            link.incubator.count = 0;
            final TreeMap<Double, TestSpecie> map = new TreeMap<>();
            final Random random = new Random();
            for (int i = 0; i < 120; i++) {
                map.put(random.nextDouble(), new TestSpecie());
            }
            for (final Map.Entry<Double, TestSpecie> e : map.entrySet()) {
                link.incubator.put(e.getKey(), e.getValue());
            }
            assertArrayEquals(map.keySet().toArray(), stream(link.incubator.fitnesses).mapToObj(Double::valueOf).toArray());
            assertArrayEquals(map.values().toArray(), link.incubator.species);
        }
    }

    private static class TestSpecie extends Specie<TestSpecie> {

        public TestSpecie() {
            super(null);
        }

        @Override
        public void paint(Graphics2D g) {

        }

        @Override
        public double fitness(GaContext gaContext) {
            return 0;
        }

        @Override
        public void mutate(GaContext gaContext) {

        }

        @Override
        public TestSpecie crossover(GaContext gaContext, TestSpecie that) {
            return null;
        }
    }
}
