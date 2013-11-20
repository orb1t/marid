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

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;
import static org.marid.util.StringUtils.*;

/**
 * @author Dmitry Ovchinnikov
 */
@Category(NormalTests.class)
public class StringUtilsTest {

    @Test
    public void testRepeated() {
        assertEquals("aaaa", repeated('a', 4));
        assertEquals("abab", repeated("ab", 2));
        assertEquals("", repeated('a', 0));
        assertEquals("", repeated("ab", 0));
    }

    @Test
    public void testDelimited() {
        assertEquals("a,b,c", delimited(',', "a", "b", "c"));
        assertEquals("a  b  c", delimited("  ", "a", "b", "c"));
        assertEquals("a", delimited(',', "a"));
        assertEquals("", delimited(','));
        assertEquals("1,2.3,4.5,q", delimited(',', 1, 2.3, 4.5f, 'q'));
        assertEquals("1++2.3++4.5++q", delimited("++", 1, 2.3, 4.5f, 'q'));
    }

    @Test
    public void testPatoi() {
        for (int i = 0; i < 1000; i++) {
            assertEquals(i, patoi(Integer.toString(i).getBytes(StandardCharsets.ISO_8859_1)));
        }
    }
}
