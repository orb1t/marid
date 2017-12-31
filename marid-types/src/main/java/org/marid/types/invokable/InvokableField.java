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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.stream.Stream;

public class InvokableField implements Invokable {

  private final boolean setter;
  private final Field field;

  public InvokableField(boolean setter, Field field) {
    this.setter = setter;
    this.field = field;
  }

  @Override
  public Object execute(Object self, Object... args) throws ReflectiveOperationException {
    if (setter) {
      field.set(self, args[0]);
      return null;
    } else {
      return field.get(self);
    }
  }

  @Override
  public boolean isStatic() {
    return Modifier.isStatic(field.getModifiers());
  }

  @NotNull
  @Override
  public Type getReturnType() {
    return setter ? Object.class : field.getGenericType();
  }

  @NotNull
  @Override
  public Type[] getParameterTypes() {
    return setter ? new Type[] {field.getGenericType()} : new Type[0];
  }

  @NotNull
  @Override
  public Class<?>[] getParameterClasses() {
    return setter ? new Class<?>[] {field.getType()} : new Class<?>[0];
  }

  @NotNull
  @Override
  public Class<?> getReturnClass() {
    return setter ? Object.class : field.getType();
  }

  @Override
  public int getParameterCount() {
    return setter ? 1 : 0;
  }

  @NotNull
  public static Stream<InvokableField> invokables(@NotNull Field field) {
    return Stream.of(new InvokableField(false, field), new InvokableField(true, field));
  }
}
