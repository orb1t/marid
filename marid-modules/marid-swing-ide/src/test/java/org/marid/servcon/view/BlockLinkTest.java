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

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.marid.servcon.view.ga.LineSpecie;
import org.marid.test.NormalTests;

import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/**
 * @author Dmitry Ovchinnikov.
 */
@Category({NormalTests.class})
public class BlockLinkTest {

    @Test
    public void testAddElements() {
        final BlockLink<LineSpecie> link = new BlockLink<>(30, LineSpecie::new, LineSpecie[]::new, null, null);
        final BlockLink<LineSpecie>.Incubator incubator = link.createIncubator(4);
        for (int iter = 0; iter < 10; iter++) {
            incubator.count = 0;
            final TreeMap<Double, LineSpecie> lineSpecieTreeMap = new TreeMap<>();
            final Random random = new Random();
            for (int i = 0; i < 120; i++) {
                lineSpecieTreeMap.put(random.nextDouble(), EasyMock.createMock(LineSpecie.class));
            }
            for (final Map.Entry<Double, LineSpecie> e : lineSpecieTreeMap.entrySet()) {
                incubator.put(e.getKey(), e.getValue());
            }
            Assert.assertArrayEquals(lineSpecieTreeMap.keySet().toArray(), Arrays.stream(incubator.fitnesses).mapToObj(Double::valueOf).toArray());
            Assert.assertArrayEquals(lineSpecieTreeMap.values().toArray(), incubator.species);
        }
    }
}
