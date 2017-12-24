package org.marid.types.invokable;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;

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
}
