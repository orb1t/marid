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
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.util.LinkedHashSet;
import java.util.stream.Stream;

/**
 * Utilities for classes.
 *
 * @since 0.9.7.
 */
public interface Classes {

  static Stream<Class<?>> classes(@NotNull Class<?> type) {
    return classes(type, true);
  }

  @NotNull
  static Stream<Class<?>> classes(@NotNull Class<?> type, boolean accessible) {
    final LinkedHashSet<Class<?>> set = new LinkedHashSet<>();
    addClasses(type, set, accessible);
    return set.stream();
  }

  private static void addClasses(@NotNull Class<?> type, @NotNull LinkedHashSet<Class<?>> classes, boolean accessible) {
    if (type.isInterface()) {
      if (!accessible || isAccessible(type)) {
        classes.add(type);
      }
      for (final Class<?> i : type.getInterfaces()) {
        addClasses(i, classes, accessible);
      }
    } else {
      for (Class<?> c = type; c != null; c = c.getSuperclass()) {
        if (!accessible || isAccessible(c)) {
          classes.add(c);
        }
      }
      for (Class<?> c = type; c != null; c = c.getSuperclass()) {
        for (final Class<?> i : c.getInterfaces()) {
          addClasses(i, classes, accessible);
        }
      }
    }
  }

  static boolean isAccessible(@NotNull Class<?> type) {
    try {
      MethodHandles.publicLookup().accessClass(type);
      return true;
    } catch (IllegalAccessException | SecurityException x) {
      return false;
    }
  }

  @NotNull
  static Class<?> wrapper(@NotNull Class<?> type) {
    switch (type.getName()) {
      case "int": return Integer.class;
      case "long": return Long.class;
      case "boolean": return Boolean.class;
      case "short": return Short.class;
      case "byte": return Byte.class;
      case "char": return Character.class;
      case "float": return Float.class;
      case "double": return Double.class;
      case "void": return Void.class;
      default: return type;
    }
  }

  static Object value(@NotNull Class<?> type, @Nullable Object value) {
    if (type.isPrimitive()) {
      if (value == null) {
        switch (type.getName()) {
          case "int": return 0;
          case "long": return 0L;
          case "boolean": return false;
          case "short": return (short) 0;
          case "byte": return (byte) 0;
          case "char": return (char) 0;
          case "float": return 0f;
          case "double": return 0d;
          default: throw new IllegalArgumentException(type.getName());
        }
      } else {
        return value;
      }
    } else if (value == null) {
      return null;
    } else if (type.isArray()) {
      final int len = Array.getLength(value);
      final Object array = Array.newInstance(type.getComponentType(), len);
      for (int i = 0; i < len; i++) {
        Array.set(array, i, value(type.getComponentType(), Array.get(value, i)));
      }
      return array;
    } else {
      return value;
    }
  }

  static Object[] args(@NotNull Executable executable, @NotNull Object[] args) {
    final Class<?>[] types = executable.getParameterTypes();
    final Object[] result = new Object[args.length];
    for (int i = 0; i < types.length; i++) {
      result[i] = value(types[i], args[i]);
    }
    return result;
  }

  @NotNull
  static Class<?> loadClass(@NotNull String name, @NotNull ClassLoader classLoader) throws ClassNotFoundException {
    switch (name) {
      case "int": return int.class;
      case "long": return long.class;
      case "boolean": return boolean.class;
      case "short": return short.class;
      case "byte": return byte.class;
      case "char": return char.class;
      case "float": return float.class;
      case "double": return double.class;
      case "void": return void.class;
      default: return Class.forName(name, false, classLoader);
    }
  }

  @NotNull
  static Stream<Method> allClassMethods(@NotNull Class<?> type) {
    return type.isInterface() || type == Object.class
        ? Stream.empty()
        : Stream.concat(Stream.of(type.getDeclaredMethods()), allClassMethods(type.getSuperclass()));
  }

  @NotNull
  static Stream<Field> allClassFields(@NotNull Class<?> type) {
    return type.isInterface() || type == Object.class
        ? Stream.empty()
        : Stream.concat(Stream.of(type.getDeclaredFields()), allClassFields(type.getSuperclass()));
  }

  @NotNull
  static Stream<Member> allClassMembers(@NotNull Class<?> type) {
    return Stream.concat(allClassMethods(type), allClassFields(type));
  }
}
