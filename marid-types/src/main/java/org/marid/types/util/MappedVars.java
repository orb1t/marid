/*-
 * #%L
 * marid-types
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

package org.marid.types.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

public final class MappedVars {

  private TypeVariable<?>[] vars = new TypeVariable<?>[0];
  private Type[] types = new Type[0];

  public void put(@NotNull TypeVariable<?> var, @NotNull Type type) {
    put(var, old -> type);
  }

  public void put(@NotNull TypeVariable<?> var, @NotNull UnaryOperator<Type> mergeOp) {
    for (int i = 0; i < vars.length; i++) {
      if (vars[i].equals(var)) {
        types[i] = mergeOp.apply(types[i]);
        return;
      }
    }
    vars = Arrays.copyOf(vars, vars.length + 1);
    types = Arrays.copyOf(types, types.length + 1);
    vars[vars.length - 1] = var;
    types[types.length - 1] = mergeOp.apply(null);
  }

  @Nullable
  public Type get(@NotNull TypeVariable<?> var) {
    for (int i = 0; i < vars.length; i++) {
      if (vars[i].equals(var)) {
        return types[i];
      }
    }
    return null;
  }

  public Stream<TypeVariable<?>> vars() {
    return Stream.of(vars);
  }

  public Stream<Type> types() {
    return Stream.of(types);
  }

  @NotNull
  public Stream<Map.Entry<TypeVariable<?>, Type>> entries() {
    return IntStream.range(0, vars.length).mapToObj(i -> Map.entry(vars[i], types[i]));
  }

  @NotNull
  public Stream<Map.Entry<TypeVariable<?>, Type>> reversedEntries() {
    return IntStream.range(0, vars.length).map(i -> vars.length - i - 1).mapToObj(i -> Map.entry(vars[i], types[i]));
  }

  public void forEachReversed(@NotNull BiConsumer<TypeVariable<?>, Type> consumer) {
    for (int i = vars.length - 1; i >= 0; i--) {
      consumer.accept(vars[i], types[i]);
    }
  }

  @Override
  public String toString() {
    return entries().map(MappedVars::entryToString).collect(joining(",", "{", "}"));
  }

  private static String entryToString(@NotNull Map.Entry<TypeVariable<?>, Type> entry) {
    return entry.getKey() + "(" + entry.getKey().getGenericDeclaration() + "): " + entry.getValue();
  }
}
