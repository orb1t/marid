/*-
 * #%L
 * marid-runtime
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.marid.runtime;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.marid.runtime.converter.DefaultValueConverters;
import org.marid.test.NormalTests;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author Dmitry Ovchinnikov
 */
@Category({NormalTests.class})
public class DefaultValueConvertersTest {

    private static DefaultValueConverters converters;

    private static Set<String> stringSetTypeField;
    private static SortedSet<String> sortedStringSetTypeField;

    @BeforeClass
    public static void init() {
        converters = new DefaultValueConverters();
    }

    @Test
    public void testString() {
        final Function<String, ?> function = converters.getConverter(String.class);
        assertEquals("a", function.apply("a"));
    }

    @Test
    public void testIntArray() {
        final Function<String, ?> function = converters.getConverter(int[].class);
        assertArrayEquals(new int[] {1, 2}, (int[]) function.apply("1, 2"));
    }

    @Test
    public void testStringSet() throws Exception {
        final Type type = DefaultValueConvertersTest.class.getDeclaredField("stringSetTypeField").getGenericType();
        final Function<String, ?> function = converters.getConverter(type);
        assertEquals(new HashSet<>(asList("a", "b")), function.apply("a, b"));
    }

    @Test
    public void testSortedSet() throws Exception {
        final Type type = DefaultValueConvertersTest.class.getDeclaredField("sortedStringSetTypeField").getGenericType();
        final Function<String, ?> function = converters.getConverter(type);
        assertArrayEquals(new Object[] {"a", "b"}, ((Set) function.apply("b, a")).toArray());
    }
}
