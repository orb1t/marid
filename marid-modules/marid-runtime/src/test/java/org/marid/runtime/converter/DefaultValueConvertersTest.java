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

package org.marid.runtime.converter;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.marid.runtime.context.MaridRuntime;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Dmitry Ovchinnikov
 */
@Tag("normal")
public class DefaultValueConvertersTest {

    private static final DefaultValueConvertersFactory FACTORY = new DefaultValueConvertersFactory();
    private static final ValueConverters CONVERTERS = FACTORY.converters(Mockito.mock(MaridRuntime.class));

    @Test
    public void testString() {
        final ValueConverter converter = CONVERTERS.getConverter("of");
        assertEquals("a", converter.convert("a", String.class));
    }

    @Test
    public void testIntArray() {
        final ValueConverter function = CONVERTERS.getConverter("int[]");
        assertArrayEquals(new int[] {1, 2}, (int[]) function.convert("1, 2", int[].class));
    }
}
