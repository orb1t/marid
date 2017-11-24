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

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

public interface MaridSets {

  @SuppressWarnings("unchecked")
  @Nonnull
  static <E> Set<E> add(@Nonnull Set<E> set, @Nonnull E element) {
    if (set.isEmpty()) {
      return Set.of(element);
    } else if (set.contains(element)) {
      return set;
    } else {
      switch (set.size()) {
        case 1: {
          final Iterator<E> i = set.iterator();
          return Set.of(i.next(), element);
        }
        case 2: {
          final Iterator<E> i = set.iterator();
          return Set.of(i.next(), i.next(), element);
        }
        default: return (Set<E>) Set.of(Stream.concat(set.stream(), Stream.of(element)).toArray());
      }
    }
  }
}
