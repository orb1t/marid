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

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.marid.runtime.context.MaridRuntime;
import org.marid.runtime.converter.DefaultValueConvertersFactory;
import org.marid.runtime.converter.ValueConverters;
import org.marid.test.NormalTests;
import org.mockito.Mockito;

import java.util.function.Function;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author Dmitry Ovchinnikov
 */
@Category({NormalTests.class})
public class DefaultValueConvertersTest {

    private static final DefaultValueConvertersFactory FACTORY = new DefaultValueConvertersFactory();
    private static final ValueConverters CONVERTERS = FACTORY.converters(Mockito.mock(MaridRuntime.class));

    @Test
    public void testString() {
        final Function<String, ?> function = CONVERTERS.getConverter("String");
        assertEquals("a", function.apply("a"));
    }

    @Test
    public void testIntArray() {
        final Function<String, ?> function = CONVERTERS.getConverter("int[]");
        assertArrayEquals(new int[] {1, 2}, (int[]) function.apply("1, 2"));
    }
}
