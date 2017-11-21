package org.marid.types;

import javax.annotation.Nonnull;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.function.BiConsumer;

import static org.marid.types.Types.*;

final class TypeEvaluator implements BiConsumer<Type, Type> {

  private final HashSet<Type> passed = new HashSet<>();
  private final LinkedHashMap<TypeVariable<?>, LinkedHashSet<Type>> typeMappings = new LinkedHashMap<>();

  @Override
  public void accept(Type formal, Type actual) {
    if (formal instanceof TypeVariable<?>) {
      final TypeVariable<?> typeVariable = (TypeVariable<?>) formal;
      for (final Type bound : typeVariable.getBounds()) {
        accept(bound, actual);
      }
      put(typeVariable, actual);
    } else if (passed.add(formal)) {
      if (Types.isArrayType(formal) && Types.isArrayType(actual)) {
        accept(getArrayComponentType(formal), getArrayComponentType(actual));
      } else if (formal instanceof ParameterizedType) {
        final ParameterizedType p = (ParameterizedType) formal;
        final Map<TypeVariable<?>, Type> map = resolveVars(actual);
        final ParameterizedType resolved = (ParameterizedType) resolve(formal, map);
        final Type[] formals = p.getActualTypeArguments();
        final Type[] actuals = resolved.getActualTypeArguments();
        for (int i = 0; i < formals.length; i++) {
          accept(formals[i], actuals[i]);
        }
      } else if (formal instanceof WildcardType) {
        final WildcardType wildcardType = (WildcardType) formal;
        for (final Type bound : wildcardType.getUpperBounds()) {
          accept(bound, actual);
        }
      }
    }
  }

  @Nonnull
  Type eval(@Nonnull Type type) {
    final LinkedHashMap<TypeVariable<?>, Type> mapping = new LinkedHashMap<>(typeMappings.size());
    typeMappings.forEach((k, v) -> mapping.put(k, v.stream().reduce(Types::nct).orElse(k)));
    return ground(type, mapping);
  }

  private void put(TypeVariable<?> variable, Type type) {
    typeMappings.computeIfAbsent(variable, k -> new LinkedHashSet<>()).add(boxed(type));
  }
}
