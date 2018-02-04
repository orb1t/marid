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

package org.marid.types;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Optional;

public class TypeContext {

  public <E extends Throwable> void throwError(@NotNull E exception) throws E {
    throw exception;
  }

  @NotNull
  public ClassLoader getClassLoader() {
    return Thread.currentThread().getContextClassLoader();
  }

  @NotNull
  public Type resolve(@Nullable Type owner, @NotNull Type type) {
    if (owner == null) {
      return type;
    } else {
      return Types.resolve(type, Types.resolveVars(owner));
    }
  }

  @NotNull
  public Optional<Class<?>> getClass(@NotNull String name) {
    try {
      return Optional.of(Classes.loadClass(name, getClassLoader()));
    } catch (RuntimeException x) {
      throwError(x);
    } catch (Exception x) {
      throwError(new IllegalStateException(x));
    }
    return Optional.empty();
  }
}
