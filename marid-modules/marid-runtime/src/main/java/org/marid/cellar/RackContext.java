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

package org.marid.cellar;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.marid.cellar.runtime.RuntimeBottle;
import org.marid.cellar.runtime.RuntimeRack;
import org.marid.collections.MaridIterators;
import org.marid.collections.MaridMaps;
import org.marid.runtime.exception.BottleDestructionException;
import org.marid.runtime.exception.BottleInitializationException;
import org.marid.runtime.exception.RackContextCloseException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class RackContext implements AutoCloseable {

  @Nullable
  private final RackContext parent;

  @NotNull
  private final String name;

  @NotNull
  private final LinkedHashMap<@NotNull String, @Nullable Object> bottleMap;

  @NotNull
  private final Map<@NotNull String, @NotNull Runnable> destroyerMap;

  @NotNull
  private final ArrayList<@NotNull RackContext> children;

  public RackContext(@Nullable RackContext parent, @NotNull RuntimeRack rack, @NotNull CellarContext configuration) {
    this.parent = parent;
    this.name = rack.getName();
    this.bottleMap = new LinkedHashMap<>(rack.getBottles().size(), 0f);
    this.children = new ArrayList<>(rack.getSubRacks().size());

    final HashMap<String, Runnable> destroyerMap = new HashMap<>(rack.getBottles().size(), 0f);
    final ClassLoader classLoader = configuration.getClassLoader();
    for (final RuntimeBottle bottle : rack.getBottles()) {
      final Object instance;
      try {
        instance = bottle.getFactory().evaluate(null, null, new ExecutionContext(bottle, this, classLoader));
      } catch (Throwable x) {
        final BottleInitializationException e = new BottleInitializationException(this, bottle, x);
        try {
          close();
        } catch (Throwable cx) {
          e.addSuppressed(cx);
        }
        throw e;
      }

      bottleMap.put(bottle.getName(), instance);

      try {
        destroyerMap.computeIfAbsent(bottle.getName(), k -> configuration.destroyer(bottle, this, instance));
        configuration.fireInitializers(bottle, this, instance);
      } catch (Exception x) {
        try {
          close();
        } catch (Throwable cx) {
          x.addSuppressed(cx);
        }
        throw x;
      }
    }

    this.destroyerMap = MaridMaps.immutable(destroyerMap);
  }

  @Nullable
  public RackContext getParent() {
    return parent;
  }

  @NotNull
  public String getName() {
    return name;
  }

  public boolean containsBottle(@NotNull String name) {
    return bottleMap.containsKey(name);
  }

  @Nullable
  public Object getBottle(@NotNull String name) {
    return bottleMap.get(name);
  }

  @NotNull
  public Stream<@NotNull RackContext> parents() {
    return Stream.ofNullable(parent).flatMap(p -> Stream.concat(Stream.of(p), p.parents()));
  }

  @Override
  public void close() throws Exception {
    final LinkedList<Throwable> errors = new LinkedList<>();

    for (int i = children.size() - 1; i >= 0; i--) {
      final RackContext child = children.get(i);
      try {
        child.close();
      } catch (Throwable x) {
        errors.add(x);
      } finally {
        children.remove(i);
      }
    }

    final LinkedList<Map.Entry<String, Object>> bottles = new LinkedList<>(bottleMap.entrySet());
    for (final Map.Entry<String, Object> entry : MaridIterators.iterable(bottles::descendingIterator)) {

      final String bottleName = entry.getKey();
      final Object bottle = entry.getValue();

      try {
        final Runnable destroyer = destroyerMap.get(bottleName);
        if (destroyer != null) {
          destroyer.run();
        }
      } catch (Throwable x) {
        errors.add(x);
      }

      if (bottle instanceof AutoCloseable) {
        try {
          ((AutoCloseable) bottle).close();
        } catch (Throwable x) {
          errors.add(new BottleDestructionException(this, bottleName, x));
        }
      }

      bottleMap.remove(bottleName);
    }

    if (!errors.isEmpty()) {
      throw new RackContextCloseException(this, errors);
    }
  }

  @Override
  public String toString() {
    return Stream.concat(Stream.of(this), parents()).map(RackContext::getName).collect(Collectors.joining("/"));
  }
}
