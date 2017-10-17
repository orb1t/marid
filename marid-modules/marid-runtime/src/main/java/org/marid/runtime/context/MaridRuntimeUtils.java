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

import org.marid.misc.StringUtils;
import org.marid.runtime.exception.MaridUnknownSignatureException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.*;
import java.util.Comparator;
import java.util.Scanner;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static java.lang.invoke.MethodHandles.publicLookup;
import static java.util.logging.Level.WARNING;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.of;
import static org.marid.logging.Log.log;
import static org.marid.misc.Calls.call;

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

    static Thread daemonThread(AtomicReference<BeanContext> contextRef) {
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
                                final BeanContext context = contextRef.get();
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

    @Nonnull
    static String signature(@Nonnull Field field) {
        return String.format("F %04X %s %s",
                field.getModifiers(),
                field.getDeclaringClass().getCanonicalName(),
                field.getName()
        );
    }

    @Nonnull
    static String signature(@Nonnull Method method) {
        return String.format("M %04X %s %s %s",
                method.getModifiers(),
                method.getDeclaringClass().getCanonicalName(),
                method.getName(),
                args(method)
        );
    }

    @Nonnull
    static String signature(@Nonnull Constructor<?> constructor) {
        return String.format("C %04X %s %s",
                constructor.getModifiers(),
                constructor.getDeclaringClass().getCanonicalName(),
                args(constructor)
        );
    }

    @Nonnull
    static String args(@Nonnull Executable executable) {
        return of(executable.getParameterTypes()).map(Class::getCanonicalName).collect(joining(","));
    }

    @Nonnull
    static String toCanonical(@Nonnull String signature) {
        final int limit = StringUtils.count(signature, ' ') + 1;
        final String[] parts = signature.split(" ", limit);
        final String mods = Modifier.toString(Integer.parseUnsignedInt(parts[1], 16));
        switch (parts[0]) {
            case "F": return String.format("%s %s.%s", mods, parts[2], parts[3]);
            case "C": return String.format("%s %s(%s)", mods, parts[2], parts[3]);
            case "M": return String.format("%s %s.%s(%s)", mods, parts[2], parts[3], parts[4]);
            default: throw new IllegalArgumentException(signature);
        }
    }

    @Nonnull
    static String toCanonicalWithArgs(@Nonnull String signature, Type... types) {
        final int limit = StringUtils.count(signature, ' ') + 1;
        final String[] parts = signature.split(" ", limit);
        final String args = of(types).map(Type::getTypeName).collect(joining(","));
        switch (parts[0]) {
            case "F": return String.format("%s.%s", parts[2], parts[3]);
            case "C": return String.format("%s(%s)", parts[2], args);
            case "M": return String.format("%s.%s(%s)", parts[2], parts[3], args);
            default: throw new IllegalArgumentException(signature);
        }
    }

    static int modifiers(@Nonnull String signature) {
        final int limit = StringUtils.count(signature, ' ') + 1;
        final String[] parts = signature.split(" ", limit);
        return Integer.parseUnsignedInt(parts[1], 16);
    }

    static Member fromSignature(@Nonnull String signature, @Nonnull ClassLoader classLoader) {
        try {
            final int limit = StringUtils.count(signature, ' ') + 1;
            final String[] parts = signature.split(" ", limit);
            final Class<?> declaringClass = Class.forName(parts[2], false, classLoader);
            switch (parts[0]) {
                case "F":
                    return of(declaringClass.getFields())
                            .filter(f -> f.getName().equals(parts[3]))
                            .findFirst()
                            .orElseThrow(() -> new NoSuchFieldException(parts[3]));
                case "C":
                    return of(declaringClass.getConstructors())
                            .filter(c -> args(c).equals(parts[3]))
                            .findFirst()
                            .orElseThrow(() -> new NoSuchMethodException(signature));
                case "M":
                    return of(declaringClass.getMethods())
                            .filter(m -> m.getName().equals(parts[3]))
                            .filter(m -> args(m).equals(parts[4]))
                            .findFirst()
                            .orElseThrow(() -> new NoSuchMethodException(parts[3]));
                default:
                    throw new IllegalArgumentException(parts[0]);
            }
        } catch (Throwable x) {
            throw new MaridUnknownSignatureException(signature, x);
        }
    }

    static boolean isRoot(Member member) {
        return member instanceof Constructor<?> || Modifier.isStatic(member.getModifiers());
    }

    static MethodHandle producer(Member member) {
        if (member instanceof Constructor<?>) {
            return call(() -> publicLookup().unreflectConstructor((Constructor<?>) member));
        } else if (member instanceof Method) {
            return call(() -> publicLookup().unreflect((Method) member));
        } else {
            return call(() -> publicLookup().unreflectGetter((Field) member));
        }
    }

    static MethodHandle initializer(Member member) {
        if (member instanceof Constructor<?>) {
            return call(() -> publicLookup().unreflectConstructor((Constructor<?>) member));
        } else if (member instanceof Method) {
            return call(() -> publicLookup().unreflect((Method) member));
        } else {
            return call(() -> publicLookup().unreflectSetter((Field) member));
        }
    }

    static @Nullable Object value(@Nonnull Class<?> target, @Nullable Object v) {
        if (target.isPrimitive()) {
            if (v == null) {
                switch (target.getName()) {
                    case "int": return 0;
                    case "long": return 0L;
                    case "short": return (short) 0;
                    case "byte": return (byte) 0;
                    case "char": return (char) 0;
                    case "double": return 0d;
                    case "float": return 0f;
                    case "boolean": return false;
                    default: throw new IllegalArgumentException(target.getName());
                }
            } else {
                return v;
            }
        } else {
            return v;
        }
    }

    static boolean compatible(@Nonnull Executable executable, @Nonnull Object... args) {
        if (executable.getParameterCount() == args.length) {
            final Class<?>[] types = executable.getParameterTypes();
            for (int i = 0; i < args.length; i++) {
                final Class<?> type = types[i];
                if (args[i] != null && !type.isInstance(args[i])) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }
}
