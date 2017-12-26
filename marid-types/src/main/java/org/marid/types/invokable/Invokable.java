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
import org.marid.types.Types;

import java.lang.reflect.Type;
import java.util.function.Function;
import java.util.stream.Stream;

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
}
