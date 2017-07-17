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

package org.marid.runtime.beans;

import org.marid.io.Xmls;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.*;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.of;
import static org.marid.misc.Builder.build;

/**
 * @author Dmitry Ovchinnikov
 */
public final class Bean {

    @Nonnull
    public final String name;

    @Nonnull
    public final String factory;

    @Nonnull
    public final String producer;

    @Nonnull
    public final BeanMember[] args;

    @Nonnull
    public final BeanMember[] props;

    public Bean(@Nonnull String name,
                @Nonnull String factory,
                @Nonnull String producer,
                @Nonnull BeanMember[] args,
                @Nonnull BeanMember[] props) {
        this.name = name;
        this.factory = factory;
        this.producer = producer;
        this.args = args;
        this.props = props;
    }

    public Bean(@Nonnull Element element) {
        this.name = requireNonNull(element.getAttribute("name"));
        this.factory = requireNonNull(element.getAttribute("factory"));
        this.producer = requireNonNull(element.getAttribute("producer"));
        this.args = Xmls.nodes(element, Element.class)
                .filter(e -> "args".equals(e.getTagName()))
                .flatMap(e -> Xmls.nodes(e, Element.class))
                .filter(e -> "arg".equals(e.getTagName()))
                .map(BeanMember::new)
                .toArray(BeanMember[]::new);
        this.props = Xmls.nodes(element, Element.class)
                .filter(e -> "props".equals(e.getTagName()))
                .flatMap(e -> Xmls.nodes(e, Element.class))
                .filter(e -> "prop".equals(e.getTagName()))
                .map(BeanMember::new)
                .toArray(BeanMember[]::new);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, factory, producer, args, props);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null) {
            return true;
        } else if (obj.getClass() != Bean.class) {
            return false;
        } else {
            final Bean that = (Bean) obj;
            return Arrays.deepEquals(
                    new Object[]{this.name, this.factory, this.producer, this.args, this.props},
                    new Object[]{that.name, that.factory, that.producer, that.args, that.props}
            );
        }
    }

    public void writeTo(@Nonnull Element element) {
        element.setAttribute("name", name);
        element.setAttribute("factory", factory);
        element.setAttribute("producer", producer);

        build(element.getOwnerDocument().createElement("args"), argsElement -> {
            element.appendChild(argsElement);
            for (final BeanMember arg : args) {
                build(element.getOwnerDocument().createElement("arg"), e -> {
                    argsElement.appendChild(e);
                    arg.writeTo(e);
                });
            }
        });

        build(element.getOwnerDocument().createElement("props"), argsElement -> {
            element.appendChild(argsElement);
            for (final BeanMember prop : props) {
                build(element.getOwnerDocument().createElement("prop"), e -> {
                    argsElement.appendChild(e);
                    prop.writeTo(e);
                });
            }
        });
    }

    private boolean matchArgs(String[] args, Executable executable) {
        if (args.length == executable.getParameterCount()) {
            final Class<?>[] argTypes = executable.getParameterTypes();
            for (int i = 0; i < args.length; i++) {
                if (!args[i].equals(argTypes[i].getName())) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public BeanConstructor findProducer(Entry<String, String[]> p, Class<?> type, Object target) {
        final Lookup l = MethodHandles.publicLookup();
        try {
            switch (p.getKey()) {
                case "new": {
                    for (final Constructor<?> c : type.getConstructors()) {
                        if (matchArgs(p.getValue(), c)) {
                            return new BeanConstructor(type, c.getGenericParameterTypes(), l.unreflectConstructor(c));
                        }
                    }
                    break;
                }
                default: {
                    for (final Method m : type.getMethods()) {
                        if (m.getName().equals(p.getKey()) && matchArgs(p.getValue(), m)) {
                            MethodHandle h = l.unreflect(m);
                            if (!Modifier.isStatic(m.getModifiers())) {
                                h = h.bindTo(target);
                            }
                            return new BeanConstructor(m.getGenericReturnType(), m.getGenericParameterTypes(), h);
                        }
                    }
                    if (args.length == 0) {
                        for (final Field f : type.getFields()) {
                            if (f.getName().equals(p.getKey())) {
                                MethodHandle h = l.unreflectGetter(f);
                                if (!Modifier.isStatic(f.getModifiers())) {
                                    h = h.bindTo(target);
                                }
                                return new BeanConstructor(f.getGenericType(), h.type().parameterArray(), h);
                            }
                        }
                    }
                    break;
                }
            }
            throw new IllegalStateException(format("Not found: %s(%s)", p.getKey(), String.join(",", p.getValue())));
        } catch (IllegalAccessException x) {
            throw new IllegalStateException(x);
        }
    }

    public BeanProperties findProperties(BeanConstructor constructor) {
        final Type[] types = new Type[props.length];
        final MethodHandle[] setters = new MethodHandle[props.length];
        final Class<?> targetClass = constructor.handle.type().returnType();

        final Function<Type, Type> genericTypeResolver = argType -> {
            if (argType instanceof TypeVariable && constructor.type instanceof ParameterizedType) {
                final ParameterizedType pt = (ParameterizedType) constructor.type;
                final Type[] typeArgs = pt.getActualTypeArguments();
                final TypeVariable<?>[] vars = targetClass.getTypeParameters();
                if (typeArgs.length == vars.length) {
                    for (int a = 0; a < vars.length; a++) {
                        if (argType.equals(vars[a])) {
                            return typeArgs[a];
                        }
                    }
                }
            }
            return argType;
        };
        final Lookup l = MethodHandles.publicLookup();
        final Function<BeanMember, Entry<MethodHandle, Type>> handleResolver = prop -> {
            try {
                for (final Method method : targetClass.getMethods()) {
                    if (method.getParameterCount() == 1 && method.getName().equals(prop.name)) {
                        final MethodHandle h = l.unreflect(method);
                        final Type t = genericTypeResolver.apply(method.getGenericParameterTypes()[0]);
                        return new SimpleImmutableEntry<>(h, t);
                    }
                }
                for (final Field field : targetClass.getFields()) {
                    if (field.getName().equals(prop.name)) {
                        final MethodHandle h = l.unreflectSetter(field);
                        final Type t = genericTypeResolver.apply(field.getGenericType());
                        return new SimpleImmutableEntry<>(h, t);
                    }
                }
                throw new IllegalStateException(format("No property found: %s", prop));
            } catch (IllegalAccessException x) {
                throw new IllegalStateException(x);
            }
        };

        for (int i = 0; i < props.length; i++) {
            final BeanMember prop = props[i];
            final Entry<MethodHandle, Type> e = handleResolver.apply(prop);
            types[i] = e.getValue();
            setters[i] = e.getKey();
        }

        return new BeanProperties(types, setters);
    }

    public static String factory(Constructor<?> constructor) {
        return of(constructor.getParameterTypes()).map(Class::getName).collect(joining(",", "new(", ")"));
    }

    public static String factory(Method method) {
        return of(method.getParameterTypes()).map(Class::getName).collect(joining(",", method.getName() + "(", ")"));
    }

    public static String factory(Field field) {
        return field.getName();
    }

    @Override
    public String toString() {
        return format("Bean(%s,%s,%s,%s,%s)",
                name,
                factory,
                producer,
                Arrays.toString(args),
                Arrays.toString(props)
        );
    }
}
