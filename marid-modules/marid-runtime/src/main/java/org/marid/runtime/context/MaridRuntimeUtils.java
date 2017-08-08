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

import org.marid.runtime.exception.MaridUnknownSignatureException;

import javax.annotation.Nonnull;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.lang.invoke.MethodHandles.publicLookup;
import static java.lang.reflect.Modifier.*;
import static java.util.logging.Level.WARNING;
import static java.util.regex.Pattern.compile;
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
        final Consumer<Class<?>> consumer = c -> Stream.of(c.getDeclaredMethods())
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

    static Thread daemonThread(AtomicReference<MaridContext> contextRef) {
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
                                final MaridContext context = contextRef.get();
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

    // TODO: speed optimization, make gc-less
    static Member fromSignature(String signature, ClassLoader classLoader) {
        final int pIndex = signature.indexOf('(');
        final int modMask = methodModifiers() | constructorModifiers() | fieldModifiers();
        final Pattern space = compile(" ");
        final String[] mods = space.splitAsStream(Modifier.toString(modMask)).sorted().toArray(String[]::new);
        final String[] tokens = space.splitAsStream(pIndex >= 0 ? signature.substring(0, pIndex) : signature)
                .filter(t -> Arrays.binarySearch(mods, t) < 0)
                .toArray(String[]::new);
        final String declaringClassName;
        try {
            if (pIndex >= 0) {
                switch (tokens.length) {
                    case 1: // constructor
                        declaringClassName = tokens[0];
                        return Stream.of(Class.forName(declaringClassName, false, classLoader).getConstructors())
                                .filter(c -> c.toString().equals(signature))
                                .findFirst()
                                .orElseThrow(() -> new NoSuchMethodException(signature));
                    default:
                        declaringClassName = tokens[1].substring(0, tokens[1].lastIndexOf('.'));
                        return Stream.of(Class.forName(declaringClassName, false, classLoader).getMethods())
                                .filter(m -> m.toString().equals(signature))
                                .findFirst()
                                .orElseThrow(() -> new NoSuchMethodException(signature));
                }
            } else {
                declaringClassName = tokens[1].substring(0, tokens[1].lastIndexOf('.'));
                return Stream.of(Class.forName(declaringClassName, false, classLoader).getFields())
                        .filter(m -> m.toString().equals(signature))
                        .findFirst()
                        .orElseThrow(() -> new NoSuchFieldException(signature));
            }
        } catch (Exception x) {
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
}
