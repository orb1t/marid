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

package org.marid.types.invokable;

import org.jetbrains.annotations.NotNull;
import org.marid.types.Classes;
import org.marid.types.MaridParameterizedType;
import org.marid.types.Types;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Arrays.stream;

public interface Invokable {

  Object execute(Object self, Object... args) throws ReflectiveOperationException;

  boolean isStatic();

  @NotNull
  Type getReturnType();

  @NotNull
  Type[] getParameterTypes();

  @NotNull
  Class<?>[] getParameterClasses();

  @NotNull
  Class<?> getReturnClass();

  int getParameterCount();

  MethodHandle toMethodHandle();

  default boolean matches(@NotNull Type... types) {
    if (getParameterCount() == types.length) {
      final Type[] args = getParameterTypes();
      final Function<Type, Stream<Class<?>>> boxed = t -> Types.rawClasses(t).map(Classes::wrapper);
      for (int i = 0; i < args.length; i++) {
        final Type formal = args[i];
        final Type actual = types[i];
        if (!boxed.apply(formal).allMatch(t -> boxed.apply(actual).anyMatch(t::isAssignableFrom))) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }

  default Type type(@NotNull Type target, @NotNull Class<?> type, @NotNull Method sam, @NotNull int[] indices, @NotNull Type... args) {
    final TypeVariable<?>[] vars = type.getTypeParameters();
    if (vars.length == 0) {
      return type;
    }
    final Type[] samArgs = sam.getGenericParameterTypes();
    final Map<TypeVariable<?>, Type> typeVars = Types.resolveVars(target);
    return Types.evaluate(e -> {
      for (int k = 0; k < indices.length; k++) {
        final int index = indices[k];
        if (index >= 0 && index < getParameterCount()) {
          final Type actual = Types.resolve(getParameterTypes()[index], typeVars);
          final Type formal = samArgs[k];
          e.bind(formal, actual);
        }
      }
      for (int k = 0; k < getParameterCount(); k++) {
        final int index = k;
        if (stream(indices).noneMatch(v -> v != index)) {
          final Type formal = getParameterTypes()[index];
          final Type actual = args[index];
          e.bind(formal, actual);
        }
      }
      e.bind(sam.getGenericReturnType(), Types.resolve(getReturnType(), typeVars));
    }, new MaridParameterizedType(null, type, vars));
  }
}
