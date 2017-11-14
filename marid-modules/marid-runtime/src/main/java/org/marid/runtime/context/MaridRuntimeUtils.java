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

package org.marid.runtime.context;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Array;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Scanner;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.lang.reflect.Modifier.isPublic;
import static java.util.logging.Level.WARNING;
import static java.util.stream.Collectors.joining;
import static java.util.stream.IntStream.range;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
public interface MaridRuntimeUtils {

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

  static Thread daemonThread(AtomicReference<? extends AutoCloseable> contextRef) {
    final Thread daemon = new Thread(null, () -> {
      final Scanner scanner = new Scanner(System.in);
      try {
        while (scanner.hasNextLine()) {
          final String line = scanner.nextLine().trim();
          if (line.isEmpty()) {
            continue;
          }
          System.err.println(line);
          switch (line) {
            case "close":
              try {
                final AutoCloseable context = contextRef.get();
                if (context != null) {
                  context.close();
                  contextRef.set(null);
                }
              } catch (Exception x) {
                x.printStackTrace();
              }
              break;
            case "exit":
              System.exit(1);
              break;
          }
        }
      } catch (Exception x) {
        log(WARNING, "Command processing error", x);
      }
    }, "repl", 96L * 1024L);
    daemon.setDaemon(true);
    return daemon;
  }

  static boolean compatible(@Nonnull Executable executable, @Nonnull Object... args) {
    if (executable.getParameterCount() == args.length) {
      final Class<?>[] ts = executable.getParameterTypes();
      return range(0, ts.length).filter(i -> args[i] != null).allMatch(i -> compatible(ts[i], args[i].getClass()));
    } else {
      return false;
    }
  }

  static boolean compatible(@Nonnull Executable executable, @Nonnull Class<?>... types) {
    if (executable.getParameterCount() == types.length) {
      final Class<?>[] ts = executable.getParameterTypes();
      return range(0, ts.length).allMatch(i -> compatible(ts[i], types[i]));
    } else {
      return false;
    }
  }

  @Nonnull
  static Stream<Class<?>> superClasses(@Nonnull Class<?> type) {
    return type.getSuperclass() == null ? of(type) : concat(of(type), superClasses(type.getSuperclass()));
  }

  static boolean isAccessible(@Nonnull Class<?> type) {
    final boolean isPublic = isPublic(type.getModifiers());
    return type.getEnclosingClass() == null ? isPublic : isPublic && isAccessible(type.getEnclosingClass());
  }

  @Nonnull
  static Stream<Method> accessibleMethods(@Nonnull Class<?> type) {
    return concat(superClasses(type), of(type.getInterfaces()))
        .flatMap(c -> of(c.getMethods()))
        .filter(m -> isAccessible(m.getDeclaringClass()))
        .distinct();
  }

  @Nonnull
  static Stream<Field> accessibleFields(@Nonnull Class<?> type) {
    return concat(superClasses(type), of(type.getInterfaces()))
        .flatMap(c -> of(c.getFields()))
        .filter(f -> isAccessible(f.getDeclaringClass()))
        .distinct();
  }

  static boolean compatible(@Nonnull Class<?> to, @Nonnull Class<?> from) {
    return to.equals(from)
        || to.isAssignableFrom(from)
        || to.isPrimitive() && compatible(wrapper(to), from)
        || from.isPrimitive() && compatible(to, wrapper(from));
  }

  @Nonnull
  static Class<?> wrapper(@Nonnull Class<?> primitiveType) {
    switch (primitiveType.getName()) {
      case "int": return Integer.class;
      case "long": return Long.class;
      case "boolean": return Boolean.class;
      case "short": return Short.class;
      case "byte": return Byte.class;
      case "char": return Character.class;
      case "float": return Float.class;
      case "double": return Double.class;
      case "void": return Void.class;
      default: throw new IllegalArgumentException(primitiveType.getName());
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
  static Class<?> loadClass(@Nonnull String name, @Nonnull ClassLoader classLoader, boolean init) throws ClassNotFoundException {
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
      default: return Class.forName(name, init, classLoader);
    }
  }

  @Nonnull
  static IllegalStateException methodState(@Nonnull String method, @Nonnull Object[] args, @Nonnull Throwable cause) {
    return new IllegalStateException(
        of(args).map(v -> v == null ? "*" : v.getClass().getName()).collect(joining(",", method + "(", ")")),
        cause
    );
  }
}
