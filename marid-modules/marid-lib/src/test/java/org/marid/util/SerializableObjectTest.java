/*
 * Copyright (C) 2013 Dmitry Ovchinnikov
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

package org.marid.util;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.marid.test.NormalTests;

import static org.junit.Assert.*;

/**
 * @author Dmitry Ovchinnikov
 */
@Category(NormalTests.class)
public class SerializableObjectTest {

    @Test
    public void testEqualityAndHashCode() {
        final X x1 = new X(1, "y");
        final X x2 = new X(1, "y");
        assertEquals(x1, x2);
        assertEquals(x1.hashCode(), x2.hashCode());
    }

    private static class X extends SerializableObject {

        private final int x;
        private final String y;

        private X(int x, String y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public String getY() {
            return y;
        }
    }
}
