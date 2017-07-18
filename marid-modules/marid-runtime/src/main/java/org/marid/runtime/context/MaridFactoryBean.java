package org.marid.runtime.context;

import org.marid.runtime.beans.Bean;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.*;
import java.util.stream.Stream;

import static java.lang.String.format;

/**
 * @author Dmitry Ovchinnikov
 */
final class MaridFactoryBean {

    private final String name;
    private final String[] argTypes;

    MaridFactoryBean(Bean bean) {
        final int index = bean.producer.indexOf('(');
        if (index < 0) {
            name = bean.producer;
            argTypes = new String[0];
        } else {
            name = bean.producer.substring(0, index);
            argTypes = Stream.of(bean.producer.substring(index + 1, bean.producer.length() - 1).split(","))
                    .map(String::trim)
                    .filter(e -> !e.isEmpty())
                    .toArray(String[]::new);
        }
    }

    private boolean matches(Executable executable) {
        if (argTypes.length == executable.getParameterCount()) {
            final Class<?>[] argTypes = executable.getParameterTypes();
            for (int i = 0; i < argTypes.length; i++) {
                if (!this.argTypes[i].equals(argTypes[i].getName())) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    MethodHandle findProducer(Class<?> type, Object target) {
        final Lookup l = MethodHandles.publicLookup();
        try {
            switch (name) {
                case "new": {
                    for (final Constructor<?> c : type.getConstructors()) {
                        if (matches(c)) {
                            return l.unreflectConstructor(c);
                        }
                    }
                    break;
                }
                default: {
                    for (final Method m : type.getMethods()) {
                        if (m.getName().equals(name) && matches(m)) {
                            return Modifier.isStatic(m.getModifiers()) ? l.unreflect(m) : l.unreflect(m).bindTo(target);
                        }
                    }
                    if (argTypes.length == 0) {
                        for (final Field f : type.getFields()) {
                            if (f.getName().equals(name)) {
                                return Modifier.isStatic(f.getModifiers())
                                        ? l.unreflectGetter(f)
                                        : l.unreflectGetter(f).bindTo(target);
                            }
                        }
                    }
                    break;
                }
            }
            throw new IllegalStateException(format("Not found: %s", this));
        } catch (IllegalAccessException x) {
            throw new IllegalStateException(x);
        }
    }
}
