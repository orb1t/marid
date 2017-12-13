/*-
 * #%L
 * marid-runtime
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

package org.marid.types;

import org.jetbrains.annotations.NotNull;
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

  @NotNull
  Type eval(@NotNull Type type) {
    final LinkedHashMap<TypeVariable<?>, Type> mapping = new LinkedHashMap<>(typeMappings.size());
    typeMappings.forEach((k, v) -> mapping.put(k, v.stream().reduce(Types::nct).orElse(k)));
    return ground(type, mapping);
  }

  private void put(TypeVariable<?> variable, Type type) {
    typeMappings.computeIfAbsent(variable, k -> new LinkedHashSet<>()).add(boxed(type));
  }
}
