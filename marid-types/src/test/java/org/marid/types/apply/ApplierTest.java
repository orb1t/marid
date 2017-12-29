/*-
 * #%L
 * marid-types
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

package org.marid.types.apply;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.marid.types.Types;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import static org.apache.commons.lang3.ArrayUtils.EMPTY_INT_ARRAY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.of;
import static org.marid.types.AuxTypeUtils.p;

@Tag("normal")
class ApplierTest {

  private static Stream<Arguments> applyData() {
    return Stream.of(
        of(Runnable.class, Runnable.class, String.class, "length", EMPTY_INT_ARRAY, new Type[0]),
        of(p(Callable.class, Integer.class), Callable.class, String.class, "length", EMPTY_INT_ARRAY, new Type[0]),
        of(p(Callable.class, p(ArrayList.class, Object.class)), Callable.class, ArrayList.class, "new", EMPTY_INT_ARRAY, new Type[0])/*,
        of(p(Callable.class, p(ArrayList.class, Integer.class)), Callable.class, ArrayList.class, "new", EMPTY_INT_ARRAY, new Type[] {p(Collection.class, Integer.class)})*/
    );
  }

  @ParameterizedTest
  @MethodSource("applyData")
  void test(Type expected, Class<?> type, Type target, String method, int[] indices, Type[] args) {
    final Type actual = Types.apply(type, target, method, indices, args);
    assertEquals(expected, actual);
  }
}
