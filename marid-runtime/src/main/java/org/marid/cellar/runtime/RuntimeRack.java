/*-
 * #%L
 * marid-runtime
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

package org.marid.cellar.runtime;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.marid.cellar.common.Rack;

import java.util.List;
import java.util.stream.Stream;

public class RuntimeRack implements Rack {

  @Nullable
  private final RuntimeRack parent;

  @NotNull
  private final String name;

  @NotNull
  private final List<@NotNull RuntimeBottle> bottles;

  @NotNull
  private final List<@NotNull RuntimeRack> subRacks;

  public RuntimeRack(@Nullable RuntimeRack parent,
                     @NotNull String name,
                     @NotNull List<@NotNull RuntimeBottle> bottles,
                     @NotNull List<@NotNull RuntimeRack> subRacks) {
    this.name = name;
    this.parent = parent;
    this.bottles = bottles;
    this.subRacks = subRacks;
  }

  @Override
  public @Nullable RuntimeRack getParent() {
    return parent;
  }

  @Override
  public @NotNull String getName() {
    return name;
  }

  @Override
  public @NotNull List<@NotNull RuntimeBottle> getBottles() {
    return bottles;
  }

  @Override
  public @NotNull List<@NotNull RuntimeRack> getSubRacks() {
    return subRacks;
  }

  @Override
  public @NotNull Stream<@NotNull RuntimeRack> parents() {
    return parent == null ? Stream.empty() : Stream.concat(Stream.of(parent), parent.parents());
  }

  @Override
  public @NotNull Stream<@NotNull RuntimeRack> subRacks() {
    return subRacks.stream().flatMap(c -> Stream.concat(Stream.of(c), c.subRacks.stream()));
  }
}
