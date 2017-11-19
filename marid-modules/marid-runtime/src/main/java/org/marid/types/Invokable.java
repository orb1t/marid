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

import javax.annotation.Nonnull;
import java.lang.reflect.Executable;
import java.lang.reflect.Type;

public abstract class Invokable<D extends Executable> {

  @Nonnull
  private final D executable;

  Invokable(@Nonnull D executable) {
    this.executable = executable;
  }

  public abstract Object execute(Object self, Object... args) throws ReflectiveOperationException;

  public abstract boolean isStatic();

  @Nonnull
  public D getExecutable() {
    return executable;
  }

  @Nonnull
  public abstract Type getReturnType();

  @Nonnull
  public abstract Type[] getParameterTypes();

  @Nonnull
  public abstract Class<?>[] getParameterClasses();

  @Nonnull
  public abstract Class<?> getReturnClass();

  public int getParameterCount() {
    return getParameterTypes().length;
  }
}
