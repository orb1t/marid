/*-
 * #%L
 * marid-types
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

package org.marid.types.apply;

import org.jetbrains.annotations.NotNull;
import org.marid.types.MaridParameterizedType;
import org.marid.types.Types;
import org.marid.types.invokable.Invokables;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class Applier {

  private final Class<?> type;
  private final Type target;
  private final String method;
  private final Type[] args;
  private final int[] indices;

  public Applier(@NotNull Class<?> type, @NotNull Type target, @NotNull String method, @NotNull Type[] args, int... indices) {
    this.type = type;
    this.target = target;
    this.method = method;
    this.args = args;
    this.indices = indices;
  }

  public Applier(@NotNull Class<?> type, @NotNull Type target, @NotNull String method, @NotNull Type[] args, @NotNull Collection<Integer> indices) {
    this(type, target, method, args, indices.stream().mapToInt(Integer::intValue).toArray());
  }


  public Type getType() {
    final TypeVariable<?>[] vars = type.getTypeParameters();
    if (vars.length == 0) {
      return type;
    } else {
      return getSam(type)
          .flatMap(m -> Types.rawClasses(target)
                .flatMap(c -> Invokables.invokables(c, method))
                .filter(i -> i.matches(args))
                .findFirst()
                .map(i -> {
                  final Type[] samArgs = m.getGenericParameterTypes();
                  final Map<TypeVariable<?>, Type> typeVars = Types.resolveVars(type);
                  return Types.evaluate(e -> {
                    for (int k = 0; k < indices.length; k++) {
                      final int index = indices[k];
                      if (index >= 0 && index < i.getParameterCount()) {
                        final Type resolvedArgType = Types.resolve(i.getParameterTypes()[index], typeVars);
                        e.accept(samArgs[k], resolvedArgType);
                      }
                    }
                  }, new MaridParameterizedType(null, type, vars));
                })
          )
          .orElse(type);
    }
  }

  public static Optional<Method> getSam(@NotNull Class<?> type) {
    final Method[] candidates = Stream.of(type.getMethods())
        .filter(m -> Modifier.isAbstract(m.getModifiers()))
        .filter(m -> !m.isDefault())
        .toArray(Method[]::new);
    return candidates.length == 1 ? Optional.of(candidates[0]) : Optional.empty();
  }
}
