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
import org.marid.cellar.common.Bottle;
import org.marid.cellar.lifecycle.Destroyer;
import org.marid.cellar.lifecycle.Initializer;
import org.marid.runtime.exception.BottleDestructionException;
import org.marid.runtime.exception.BottleInitializationException;

import java.util.LinkedList;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.stream.Stream;

public class CellarContext {

  @NotNull
  private final ClassLoader classLoader;

  @NotNull
  private final Initializer[] initializers;

  @NotNull
  private final Destroyer[] destroyers;

  public CellarContext(@NotNull ClassLoader classLoader) {
    this.classLoader = classLoader;

    final ServiceLoader<Initializer> initializers = ServiceLoader.load(Initializer.class, classLoader);
    final ServiceLoader<Destroyer> destroyers = ServiceLoader.load(Destroyer.class, classLoader);

    this.initializers = initializers.stream().map(this::extractService).toArray(Initializer[]::new);
    this.destroyers = destroyers.stream().map(this::extractService).toArray(Destroyer[]::new);
  }

  @NotNull
  private <R> R extractService(@NotNull ServiceLoader.Provider<@NotNull R> supplier) {
    try {
      return supplier.get();
    } catch (Throwable x) {
      throw new IllegalStateException("Unable to get service of " + supplier.type(), x);
    }
  }

  @NotNull
  public ClassLoader getClassLoader() {
    return classLoader;
  }

  public void fireInitializers(@NotNull Bottle bottle,
                               @NotNull RackContext context,
                               @Nullable Object instance) throws BottleInitializationException {
    for (final Initializer initializer : initializers) {
      try {
        initializer.initialize(bottle, context, instance);
      } catch (Throwable x) {
        throw new BottleInitializationException(context, bottle, x);
      }
    }
  }

  @Nullable
  public Runnable destroyer(@NotNull Bottle bottle,
                            @NotNull RackContext context,
                            @Nullable Object instance) {
    final Runnable[] destroyers = Stream.of(this.destroyers)
        .map(d -> d.destroyer(bottle, context, instance))
        .filter(Objects::nonNull)
        .toArray(Runnable[]::new);
    if (destroyers.length == 0) {
      return null;
    } else {
      final String name = bottle.getName();
      return () -> {
        final LinkedList<Throwable> errors = new LinkedList<>();
        for (final Runnable destroyer : destroyers) {
          try {
            destroyer.run();
          } catch (Throwable x) {
            errors.add(x);
          }
        }
        switch (errors.size()) {
          case 0:
            return;
          case 1:
            throw new BottleDestructionException(context, name, errors.getFirst());
          default: {
            final BottleDestructionException e = new BottleDestructionException(context, name, errors.getFirst());
            errors.removeFirst();
            errors.forEach(e::addSuppressed);
            throw e;
          }
        }
      };
    }
  }
}
