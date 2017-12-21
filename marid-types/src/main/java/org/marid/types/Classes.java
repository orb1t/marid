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
import java.lang.reflect.Array;
import java.util.LinkedHashSet;
import java.util.stream.Stream;

/**
 * Utilities for classes.
 *
 * @since 0.9.7.
 */
public interface Classes {

  /**
   * Enumerates all public accessible classes from the given type (including the argument itself).
   * @param type A type to enumerate from.
   * @return All subclasses (classes and interfaces).
   */
  @NotNull
  static Stream<Class<?>> classes(@NotNull Class<?> type) {
    return classes(type, true);
  }

  /**
   * Enumerates classes from the given type (including the argument itself).
   * @param type type to enumerate from.
   * @param accessible whether the accessibility check is performed on each class or not.
   * @return all subclasses (classes and interfaces).
   */
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

  /**
   * Checks whether the given type is accessible or not.
   * @param type A type.
   * @return Accessible flag.
   */
  static boolean isAccessible(@NotNull Class<?> type) {
    try {
      MethodHandles.publicLookup().accessClass(type);
      return true;
    } catch (IllegalAccessException | SecurityException x) {
      return false;
    }
  }

  /**
   * Returns a boxed type for the given type.
   * @param type type.
   * @return Boxed type.
   */
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

  /**
   * Returns a transformed value for the given type. If value is null and type is a primitive class then
   * returns a default value for the given type. If the type is a primitive array class and value is
   * not an array of primitives then returns an array where each element is set from the given array
   * after unboxing. Otherwise, returns the originally passed value.
   * @param type type.
   * @param value value to transform.
   * @return Transformed value.
   */
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
      if (type.getComponentType() == value.getClass().getComponentType()) {
        return value;
      } else {
        final int len = Array.getLength(value);
        final Object array = Array.newInstance(type.getComponentType(), len);
        for (int i = 0; i < len; i++) {
          Array.set(array, i, value(type.getComponentType(), Array.get(value, i)));
        }
        return array;
      }
    } else {
      return value;
    }
  }

  /**
   * Loads a class by name or returns a corresponding primitive type for the given primitive type name.
   * @param name class name.
   * @param classLoader class loader.
   * @return A class loaded by the given name.
   * @throws ClassNotFoundException if the class cannot be located by the specified class loader.
   */
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
}
