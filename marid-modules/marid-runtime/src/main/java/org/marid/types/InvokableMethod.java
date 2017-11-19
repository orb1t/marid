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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

public final class InvokableMethod extends Invokable<Method> {

  @Nonnull
  private final Type returnType;

  @Nonnull
  private final Class<?> returnClass;

  @Nonnull
  private final Type[] parameterTypes;

  @Nonnull
  private final Class<?>[] parameterClasses;

  public InvokableMethod(@Nonnull Method executable) {
    super(executable);
    returnType = executable.getGenericReturnType();
    returnClass = executable.getReturnType();
    parameterTypes = executable.getGenericParameterTypes();
    parameterClasses = executable.getParameterTypes();
  }

  @Override
  public Object execute(Object self, Object... args) throws ReflectiveOperationException {
    return getExecutable().invoke(self, args);
  }

  @Override
  @Nonnull
  public Type getReturnType() {
    return returnType;
  }

  @Override
  @Nonnull
  public Type[] getParameterTypes() {
    return parameterTypes;
  }

  @Override
  public boolean isStatic() {
    return Modifier.isStatic(getExecutable().getModifiers());
  }

  @Override
  @Nonnull
  public Class<?>[] getParameterClasses() {
    return parameterClasses;
  }

  @Override
  @Nonnull
  public Class<?> getReturnClass() {
    return returnClass;
  }
}
