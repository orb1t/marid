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
import javax.annotation.Nullable;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.lang.reflect.Modifier.isPublic;
import static java.util.stream.Stream.of;

/**
 * @author Dmitry Ovchinnikov
 */
public interface Classes {

  static TreeSet<Method> methods(@Nonnull Object bean,
                                 @Nonnull Predicate<Method> filter,
                                 @Nonnull Comparator<Method> methodComparator) {
    final TreeSet<Method> methods = new TreeSet<>(methodComparator);
    final Consumer<Class<?>> consumer = c -> of(c.getDeclaredMethods())
        .filter(m -> m.getParameterCount() == 0)
        .filter(filter)
        .peek(m -> m.setAccessible(true))
        .forEach(methods::add);
    for (Class<?> c = bean.getClass(); c != null; c = c.getSuperclass()) {
      consumer.accept(c);
    }
    for (final Class<?> c : bean.getClass().getInterfaces()) {
      consumer.accept(c);
    }
    return methods;
  }

  @Nonnull
  static Stream<Class<?>> classes(@Nonnull Class<?> type) {
    final LinkedHashSet<Class<?>> set = new LinkedHashSet<>();
    addClasses(type, set);
    return set.stream();
  }

  private static void addClasses(@Nonnull Class<?> type, @Nonnull LinkedHashSet<Class<?>> classes) {
    if (type.isInterface()) {
      ifAccessible(type, classes::add);
      for (final Class<?> i : type.getInterfaces()) {
        addClasses(i, classes);
      }
    } else {
      for (Class<?> c = type; c != null; c = c.getSuperclass()) {
        ifAccessible(c, classes::add);
      }
      for (Class<?> c = type; c != null; c = c.getSuperclass()) {
        for (final Class<?> i : c.getInterfaces()) {
          addClasses(i, classes);
        }
      }
    }
  }

  static <T> void ifAccessible(@Nonnull Class<T> type, @Nonnull Consumer<Class<T>> consumer) {
    try {
      MethodHandles.publicLookup().accessClass(type);
      consumer.accept(type);
    } catch (IllegalAccessException | SecurityException x) {
      // do nothing
    }
  }

  static boolean compatible(@Nonnull Class<?> from, @Nonnull Class<?> to) {
    return wrapper(to).isAssignableFrom(wrapper(from));
  }

  @Nonnull
  static Class<?> wrapper(@Nonnull Class<?> type) {
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

  static Object value(@Nonnull Class<?> type, @Nullable Object value) {
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

  static Object[] args(@Nonnull Executable executable, @Nonnull Object[] args) {
    final Class<?>[] types = executable.getParameterTypes();
    final Object[] result = new Object[args.length];
    for (int i = 0; i < types.length; i++) {
      result[i] = value(types[i], args[i]);
    }
    return result;
  }

  @Nonnull
  static Class<?> loadClass(@Nonnull String name, @Nonnull ClassLoader classLoader) throws ClassNotFoundException {
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

  @Nonnull
  static Stream<Class<?>> declaredClasses(@Nonnull Class<?> type) {
    return classes(type).flatMap(c -> Stream.of(c.getDeclaredClasses())).filter(c -> isPublic(c.getModifiers()));
  }

  @Nonnull
  static Optional<Class<?>> declaredClass(@Nonnull Class<?> type, @Nonnull String name) {
    return declaredClasses(type).filter(c -> c.getSimpleName().equals(name)).findFirst();
  }
}
