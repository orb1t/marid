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

package org.marid.function;

import org.marid.misc.Casts;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class ToImmutableSet<E> implements Collector<E, HashSet<E>, Set<E>> {

  @Override
  public Supplier<HashSet<E>> supplier() {
    return HashSet::new;
  }

  @Override
  public BiConsumer<HashSet<E>, E> accumulator() {
    return Set::add;
  }

  @Override
  public BinaryOperator<HashSet<E>> combiner() {
    return (s1, s2) -> s2;
  }

  @Override
  public Function<HashSet<E>, Set<E>> finisher() {
    return s -> Casts.cast(Set.of(s.toArray()));
  }

  @Override
  public Set<Characteristics> characteristics() {
    return EnumSet.of(Characteristics.UNORDERED);
  }
}
