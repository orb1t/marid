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

package org.marid.marid;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.marid.typecast.DefaultTypeCaster;
import org.marid.typecast.TypeCaster;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author Dmitry Ovchinnikov
 */
@RunWith(Parameterized.class)
public class TypeCasterTest {

    private final TypeCaster caster;

    public TypeCasterTest(TypeCaster caster) {
        this.caster = caster;
    }

    @Parameters
    public static Collection<Object[]> parameters() {
        return Arrays.asList(
                new Object[]{new DefaultTypeCaster()},
                new Object[]{TypeCaster.CASTER}
        );
    }

    @Test
    public void testNumbers() {
        assertEquals(new Integer(11), caster.cast(int.class, "11"));
        assertEquals(new Long(1L), caster.cast(long.class, "1"));
        assertEquals(new Long(1L), caster.cast(Long.class, "1"));
        assertEquals(1.1f, caster.cast(float.class, "1.1"), 0.0001);
        assertEquals(1.1, caster.cast(double.class, "1.1"), 0.0001);
    }

    @Test
    public void testArrays() {
        assertArrayEquals(new byte[]{1, 2, 3},
                caster.cast(byte[].class, Arrays.asList((byte) 1, 2, (byte) 3)));
        assertArrayEquals(new int[]{1, 2, 3},
                caster.cast(int[].class, Arrays.asList(1, 2, 3)));
        assertArrayEquals(new int[][]{{1, 2}, {3, 4, 5}},
                caster.cast(int[][].class, Arrays.asList(
                        Arrays.asList(1, 2), Arrays.asList(3, 4, 5)
                )));
        assertArrayEquals(new String[][]{{"a", "b", "1"}, null},
                caster.cast(String[][].class, Arrays.asList(
                        Arrays.asList("a", "b", 1),
                        null
                )));
        assertArrayEquals(new double[][][]{{{1.2, 2.3, 3}, {3.3}}, {{0.9}, {0.8}}},
                caster.cast(double[][][].class, Arrays.asList(
                        Arrays.asList(
                                Arrays.asList(1.2, 2.3, 3),
                                Arrays.asList(3.3)
                        ),
                        Arrays.asList(
                                Arrays.asList(0.9),
                                Arrays.asList(0.8)
                        )
                )));
    }
}
