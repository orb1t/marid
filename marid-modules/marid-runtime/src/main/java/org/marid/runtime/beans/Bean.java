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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

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

    public MethodHandle[] findProperties(MethodHandle constructor) {
        final Class<?> targetClass = constructor.type().returnType();
        final Lookup l = MethodHandles.publicLookup();
        final Function<BeanMember, MethodHandle> handleResolver = prop -> {
            try {
                for (final Method method : targetClass.getMethods()) {
                    if (method.getParameterCount() == 1 && method.getName().equals(prop.name)) {
                        return l.unreflect(method);
                    }
                }
                for (final Field field : targetClass.getFields()) {
                    if (field.getName().equals(prop.name)) {
                        return l.unreflectSetter(field);
                    }
                }
                throw new IllegalStateException(format("No property found: %s", prop));
            } catch (IllegalAccessException x) {
                throw new IllegalStateException(x);
            }
        };
        return Stream.of(props).map(handleResolver).toArray(MethodHandle[]::new);
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
