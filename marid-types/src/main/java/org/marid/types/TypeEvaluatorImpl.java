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
import java.util.*;

import static org.marid.types.Types.*;

final class TypeEvaluatorImpl implements TypeEvaluator {

  private final LinkedHashMap<TypeVariable<?>, LinkedHashSet<Type>> map = new LinkedHashMap<>();

  @Override
  public void bind(Type formal, Type actual) {
    if (!Types.isGround(formal)) {
      accept(formal, actual, Collections.emptySet());
    }
  }

  private void accept(Type formal, Type actual, Set<TypeVariable<?>> vars) {
    if (formal instanceof WildcardType) {
      final WildcardType wf = (WildcardType) formal;
      for (final Type f : wf.getUpperBounds()) {
        if (actual instanceof WildcardType) {
          final WildcardType wa = (WildcardType) actual;
          for (final Type a : wa.getUpperBounds()) {
            if (Types.isAssignable(f, a)) {
              accept(f, a, vars);
            }
          }
        } else if (Types.isAssignable(f, actual)) {
          accept(f, actual, vars);
        }
      }
    } else if (formal instanceof TypeVariable<?>) {
      final TypeVariable<?> v = (TypeVariable<?>) formal;
      if (!vars.contains(v)) {
        for (final Type bound : v.getBounds()) {
          accept(bound, actual, Sets.add(vars, v));
        }
        map.computeIfAbsent(v, k -> new LinkedHashSet<>()).add(boxed(actual));
      }
    } else {
      final Type fa = Types.getArrayComponentType(formal), aa = Types.getArrayComponentType(actual);
      if (fa != null && aa != null) {
        accept(fa, aa, vars);
      } else if (formal instanceof ParameterizedType) {
        final ParameterizedType pf = (ParameterizedType) formal;
        if (actual instanceof ParameterizedType) {
          final ParameterizedType pa = (ParameterizedType) actual;
          final Type[] fargs = pf.getActualTypeArguments(), aargs = pa.getActualTypeArguments();
          if (pa.getRawType().equals(pf.getRawType()) && fargs.length == aargs.length) {
            for (int i = 0; i < fargs.length; i++) {
              final Type f = fargs[i];
              final Type a = aargs[i];
              accept(f, a, vars);
            }
            return;
          }
        }
        final Map<TypeVariable<?>, Type> map = resolveVars(actual);
        for (final Type f : pf.getActualTypeArguments()) {
          final Type a = Types.resolve(f, map);
          accept(f, a, vars);
        }
      }
    }
  }

  @NotNull
  Type eval(@NotNull Type type) {
    final LinkedHashMap<TypeVariable<?>, Type> mapping = new LinkedHashMap<>(map.size());
    map.forEach((k, v) -> mapping.put(k, v.stream().reduce(Types::common).orElse(k)));
    return ground(type, mapping);
  }
}
