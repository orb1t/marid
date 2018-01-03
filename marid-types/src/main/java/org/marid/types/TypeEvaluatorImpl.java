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
import org.marid.types.util.MappedVars;
import org.marid.types.util.PassedVars;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.IntStream;

import static java.util.Collections.emptySet;
import static org.marid.types.Types.boxed;

final class TypeEvaluatorImpl implements TypeEvaluator {

  private final LinkedHashMap<TypeVariable<?>, Set<Type>> map = new LinkedHashMap<>();

  @Override
  public void bind(Type formal, Type actual) {
    if (!Types.isGround(formal)) {
      accept(formal, actual, PassedVars.EMPTY);
    }
  }

  private void accept(Type formal, Type actual, PassedVars vars) {
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
      final PassedVars newVars = vars.add(v);
      if (newVars != vars) {
        for (final Type bound : v.getBounds()) {
          accept(bound, actual, newVars);
        }
        map.computeIfAbsent(v, k -> new LinkedHashSet<>()).add(boxed(actual));
      }
    } else {
      final Type fa = Types.getArrayComponentType(formal), aa = Types.getArrayComponentType(actual);
      if (fa != null && aa != null) {
        accept(fa, aa, vars);
      } else if (formal instanceof ParameterizedType) {
        final ParameterizedType pf = (ParameterizedType) formal;
        final Type[] fargs = pf.getActualTypeArguments();
        Types.typesTree(actual)
            .filter(ParameterizedType.class::isInstance)
            .map(ParameterizedType.class::cast)
            .filter(a -> a.getRawType().equals(pf.getRawType()))
            .map(ParameterizedType::getActualTypeArguments)
            .filter(aargs -> aargs.length == fargs.length)
            .findFirst()
            .ifPresent(aargs -> IntStream.range(0, fargs.length).forEach(i -> accept(fargs[i], aargs[i], vars)));
      }
    }
  }

  @NotNull
  Type eval(@NotNull Type type) {
    Types.resolveVars(type).forEachReversed((v, in) -> {
      for (final Type out : map.getOrDefault(v, emptySet())) {
        bind(in, out);
      }
    });
    final MappedVars mapping = new MappedVars();
    map.forEach((k, v) -> {
      final Type commonType = v.stream().reduce(Types::common).orElse(k);
      mapping.put(k, commonType);
    });
    return Types.ground(type, mapping);
  }
}
