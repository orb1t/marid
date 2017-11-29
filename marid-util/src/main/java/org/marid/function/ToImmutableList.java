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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class ToImmutableList<E> implements Collector<E, LinkedList<E>, List<E>> {

  @Override
  public Supplier<LinkedList<E>> supplier() {
    return LinkedList::new;
  }

  @Override
  public BiConsumer<LinkedList<E>, E> accumulator() {
    return List::add;
  }

  @Override
  public BinaryOperator<LinkedList<E>> combiner() {
    return (l1, l2) -> l2;
  }

  @Override
  public Function<LinkedList<E>, List<E>> finisher() {
    return l -> Casts.cast(List.of(l.toArray()));
  }

  @Override
  public Set<Characteristics> characteristics() {
    return Collections.emptySet();
  }
}
