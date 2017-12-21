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

package org.marid.types;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Set;

import static java.util.Set.of;

interface Sets {

  /**
   * Returns a new immutable set after adding the given element to the given set.
   * @param set set
   * @param e element
   * @param <E> element type
   * @return a set after adding an element.
   */
  @SuppressWarnings("unchecked")
  @NotNull
  static <E> Set<E> add(@NotNull Set<E> set, @NotNull E e) {
    if (set.isEmpty()) {
      return of(e);
    } else if (set.contains(e)) {
      return set;
    } else {
      final Iterator<E> i = set.iterator();
      switch (set.size()) {
        case 1: return of(i.next(), e);
        case 2: return of(i.next(), i.next(), e);
        case 3: return of(i.next(), i.next(), i.next(), e);
        case 4: return of(i.next(), i.next(), i.next(), i.next(), e);
        case 5: return of(i.next(), i.next(), i.next(), i.next(), i.next(), e);
        case 6: return of(i.next(), i.next(), i.next(), i.next(), i.next(), i.next(), e);
        case 7: return of(i.next(), i.next(), i.next(), i.next(), i.next(), i.next(), i.next(), e);
        case 8: return of(i.next(), i.next(), i.next(), i.next(), i.next(), i.next(), i.next(), i.next(), e);
        case 9: return of(i.next(), i.next(), i.next(), i.next(), i.next(), i.next(), i.next(), i.next(), i.next(), e);
        default: {
          final Object[] array = new Object[set.size() + 1];

          int k = 0;
          for (E v = i.next(); i.hasNext(); v = i.next()) {
            array[k++] = v;
          }
          array[k] = e;

          return Set.of((E[]) array);
        }
      }
    }
  }
}
