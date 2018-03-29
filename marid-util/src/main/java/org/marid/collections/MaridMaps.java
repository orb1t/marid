/*-
 * #%L
 * marid-util
 * %%
 * Copyright (C) 2012 - 2018 MARID software development group
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
import org.marid.misc.Casts;

import java.util.Map;

public interface MaridMaps {

  @NotNull
  static <K, V> Map<@NotNull K, @NotNull V> immutable(@NotNull Map<@NotNull K, @NotNull V> map) {
    final Map.Entry[] entries = map.entrySet().toArray(new Map.Entry[0]);
    return Map.ofEntries(Casts.cast(entries));
  }
}
