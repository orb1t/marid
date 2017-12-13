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
import java.util.Iterator;
import java.util.Scanner;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public interface MaridIterators {

  @NotNull
  static <E> Iterator<E> iterator(@NotNull BooleanSupplier hasNext, @NotNull Supplier<E> next) {
    return new Iterator<>() {
      @Override
      public boolean hasNext() {
        return hasNext.getAsBoolean();
      }

      @Override
      public E next() {
        return next.get();
      }
    };
  }

  @NotNull
  static Iterator<String> lineIterator(@NotNull Scanner scanner) {
    return iterator(scanner::hasNextLine, scanner::nextLine);
  }

  static <E> void forEach(@NotNull Iterable<E> iterable, @NotNull BiConsumer<Boolean, E> consumer) {
    boolean hasPrevious = false;
    for (final E e : iterable) {
      consumer.accept(hasPrevious, e);
      if (!hasPrevious) {
        hasPrevious = true;
      }
    }
  }
}
