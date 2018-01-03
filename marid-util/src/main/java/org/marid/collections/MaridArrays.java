/*-
 * #%L
 * marid-util
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

package org.marid.collections;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.IntFunction;
import java.util.stream.Stream;

public interface MaridArrays {

  @SafeVarargs
  @NotNull
  static <T> T[] concat(@NotNull IntFunction<T[]> arrayFunc, @NotNull T[]... arrays) {
    final T[] result = arrayFunc.apply(Stream.of(arrays).mapToInt(a -> a.length).sum());

    int i = 0;

    for (final T[] array : arrays) {
      for (final T element : array) {
        result[i++] = element;
      }
    }

    return result;
  }

  @SafeVarargs
  @NotNull
  static <T> T[] addLast(@NotNull T[] array, T... values) {
    final T[] newArray = Arrays.copyOf(array, array.length + values.length);
    System.arraycopy(values, 0, newArray, array.length, values.length);
    return newArray;
  }

  @SafeVarargs
  @NotNull
  static <T> T[] addFirst(@NotNull T[] array, T... values) {
    final T[] newArray = Arrays.copyOf(array, array.length + values.length);
    System.arraycopy(newArray, 0, newArray, values.length, array.length);
    System.arraycopy(values, 0, newArray, 0, values.length);
    return newArray;
  }
}
