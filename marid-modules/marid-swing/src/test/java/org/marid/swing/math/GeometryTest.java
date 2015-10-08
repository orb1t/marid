/*
 * Copyright (C) 2015 Dmitry Ovchinnikov
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

package org.marid.swing.math;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.marid.test.NormalTests;

import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Dmitry Ovchinnikov
 */
@Category({NormalTests.class})
public class GeometryTest {

    @Test
    public void testDistance() {
        final ThreadLocalRandom r = ThreadLocalRandom.current();
        final int R = 10_000;
        for (int i = 0; i < 10000; i++) {
            final int x1 = r.nextInt(R), y1 = r.nextInt(R), x2 = r.nextInt(R), y2 = r.nextInt(R);
            final int actual = Geometry.distance(x1, y1, x2, y2);
            final int expected = (int) Point.distance(x1, y1, x2, y2);
            Assert.assertEquals(expected, actual);
        }
    }
}
