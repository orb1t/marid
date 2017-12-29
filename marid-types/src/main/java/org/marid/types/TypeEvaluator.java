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
    if (actual instanceof WildcardType) {
      final WildcardType w = (WildcardType) actual;
      for (final Type a : w.getUpperBounds()) {
        accept(formal, a);
      }
    } else if (formal instanceof WildcardType) {
      final WildcardType w = (WildcardType) formal;
      for (final Type f : w.getUpperBounds()) {
        accept(f, actual);
      }
    } else if (formal instanceof TypeVariable<?>) {
      final TypeVariable<?> v = (TypeVariable<?>) formal;
      for (final Type bound : v.getBounds()) {
        accept(bound, actual);
      }
      put(v, actual);
    } else if (passed.add(formal)) {
      final Type fa = Types.getArrayComponentType(formal), aa = Types.getArrayComponentType(actual);
      if (fa != null && aa != null) {
        accept(fa, aa);
      } else if (formal instanceof ParameterizedType) {
        final ParameterizedType p = (ParameterizedType) formal;
        final Map<TypeVariable<?>, Type> map = resolveVars(actual);
        for (final Type f : p.getActualTypeArguments()) {
          final Type a = Types.resolve(f, map);
          accept(f, a);
        }
      }
    }
  }

  private void accept(WildcardType formal, WildcardType actual) {
    if (formal.getUpperBounds().length == 1 && actual.getUpperBounds().length == 1) {
      accept(formal.getUpperBounds()[0], actual.getUpperBounds()[0]);
    }
  }

  @NotNull
  Type eval(@NotNull Type type) {
    final LinkedHashMap<TypeVariable<?>, Type> mapping = new LinkedHashMap<>(typeMappings.size());
    typeMappings.forEach((k, v) -> mapping.put(k, v.stream().reduce(Types::common).orElse(k)));
    return ground(type, mapping);
  }

  private void put(TypeVariable<?> variable, Type type) {
    typeMappings.computeIfAbsent(variable, k -> new LinkedHashSet<>()).add(boxed(type));
  }
}
