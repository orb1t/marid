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

package org.marid.runtime.types;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.marid.types.Classes;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("normal")
class ClassesTest {

  private static Stream<Arguments> classesTestData() {
    return Stream.of(
        Arguments.of(ArrayList.class, asList(
            ArrayList.class,
            AbstractList.class,
            AbstractCollection.class,
            Object.class,
            List.class,
            Collection.class,
            Iterable.class,
            RandomAccess.class,
            Cloneable.class,
            Serializable.class
        ))
    );
  }

  @ParameterizedTest
  @MethodSource("classesTestData")
  void testClasses(Class<?> target, List<Class<?>> expected) {
    final List<Class<?>> actual = Classes.classes(target).collect(toList());

    assertEquals(expected, actual);
  }
}
