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

package org.marid.types;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.marid.test.NormalTests;

import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.reflect.TypeUtils.parameterize;
import static org.apache.commons.lang3.reflect.TypeUtils.wildcardType;
import static org.junit.Assert.assertEquals;
import static org.marid.types.GenericTypes.getType;

/**
 * @author Dmitry Ovchinnikov.
 */
@Category(NormalTests.class)
public class GenericTypesTest {

    @Test
    public void testGenericTypesMapping() {
        assertEquals(int.class, getType("int"));
        assertEquals(parameterize(List.class, String.class), getType("List<String>"));
        assertEquals(parameterize(Map.class, String.class, Long.class), getType("Map<String, Long>"));
        assertEquals(parameterize(List.class, wildcardType().withUpperBounds(Byte.class).build()), getType("List<? extends Byte>"));
        assertEquals(getType("int"), getType(getType("int").toString()));
        assertEquals(getType("List<String>"), getType(getType("List<String>").toString()));
        assertEquals(getType("List<? extends String>"), getType(getType("List<? extends String>").toString()));
        assertEquals(getType("Map<? extends String, ? extends Byte>"), getType(getType("Map<? extends String, ? extends Byte>").toString()));
    }
}
