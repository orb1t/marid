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

package org.marid.types.invokable;

import org.jetbrains.annotations.NotNull;
import org.marid.types.MaridParameterizedType;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

public final class InvokableConstructor extends AbstractInvokable<Constructor<?>> {

  private final Type returnType;
  private final Class<?> returnClass;
  private final Type[] parameterTypes;
  private final Class<?>[] parameterClasses;

  public InvokableConstructor(@NotNull Constructor<?> constructor) {
    super(constructor);
    final Type type = constructor.getAnnotatedReturnType().getType();
    if (type instanceof Class<?>) {
      final Class<?> c = (Class<?>) type;
      final TypeVariable<?>[] variables = c.getTypeParameters();
      if (variables.length > 0) {
        returnType = new MaridParameterizedType(c.getEnclosingClass(), c, variables);
      } else {
        returnType = type;
      }
    } else {
      returnType = type;
    }
    returnClass = constructor.getDeclaringClass();
    if (isStatic()) {
      parameterTypes = constructor.getGenericParameterTypes();
      parameterClasses = constructor.getParameterTypes();
    } else {
      final Type[] types = constructor.getGenericParameterTypes();
      final Class<?>[] classes = constructor.getParameterTypes();
      parameterTypes = new Type[types.length - 1];
      parameterClasses = new Class<?>[classes.length - 1];
      System.arraycopy(types, 1, parameterTypes, 0, parameterTypes.length);
      System.arraycopy(classes, 1, parameterClasses, 0, parameterClasses.length);
    }
  }

  @Override
  public Object execute(Object self, Object... args) throws ReflectiveOperationException {
    if (isStatic()) {
      return executable.newInstance(args);
    } else {
      final Object[] newArgs = new Object[args.length + 1];
      newArgs[0] = self;
      System.arraycopy(args, 0, newArgs, 1, args.length);
      return executable.newInstance(newArgs);
    }
  }

  @Override
  public boolean isStatic() {
    final Class<?> dc = executable.getDeclaringClass();
    return dc.getEnclosingClass() == null || Modifier.isStatic(dc.getModifiers());
  }

  @Override
  @NotNull
  public Type getReturnType() {
    return returnType;
  }

  @Override
  @NotNull
  public Class<?> getReturnClass() {
    return returnClass;
  }

  @Override
  public MethodHandle toMethodHandle() {
    try {
      return MethodHandles.publicLookup().unreflectConstructor(executable);
    } catch (IllegalAccessException x) {
      throw new IllegalStateException(x);
    }
  }

  @Override
  @NotNull
  public Type[] getParameterTypes() {
    return parameterTypes;
  }

  @Override
  @NotNull
  public Class<?>[] getParameterClasses() {
    return parameterClasses;
  }
}
