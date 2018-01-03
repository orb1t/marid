package org.marid.types.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.marid.collections.MaridArrays;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

public class MappedVars {

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
    vars = MaridArrays.addLast(vars, var);
    types = MaridArrays.addLast(types, mergeOp.apply(null));
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
