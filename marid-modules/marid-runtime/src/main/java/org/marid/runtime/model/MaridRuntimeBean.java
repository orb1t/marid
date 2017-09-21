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

package org.marid.runtime.model;

import org.marid.io.Xmls;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.transform.stream.StreamResult;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.marid.io.Xmls.*;
import static org.marid.misc.Builder.build;
import static org.marid.misc.Calls.call;
import static org.marid.runtime.context.MaridRuntimeUtils.signature;

public class MaridRuntimeBean extends MaridRuntimeMethod implements MaridBean {

    private final String name;
    private final String factory;
    private final List<MaridRuntimeMethod> initializers = new ArrayList<>();
    private final List<MaridRuntimeBean> children = new ArrayList<>();

    private MaridRuntimeBean(@Nullable MaridRuntimeBean parent,
                             @Nonnull String name,
                             @Nullable String factory,
                             @Nonnull String signature,
                             @Nonnull String... arguments) {
        super(parent, signature, arguments);
        this.name = name;
        this.factory = factory;
    }

    public MaridRuntimeBean(MaridRuntimeBean parent, Element element) {
        super(parent, element);
        this.name = attribute(element, "name").orElseThrow(NullPointerException::new);
        this.factory = attribute(element, "factory").orElse(null);

        Xmls.nodes(element, Element.class)
                .filter(e -> "initializer".equals(e.getTagName()))
                .map(e -> new MaridRuntimeMethod(this, e))
                .forEach(initializers::add);

        Xmls.nodes(element, Element.class)
                .filter(e -> "bean".equals(e.getTagName()))
                .map(e -> new MaridRuntimeBean(this, e))
                .forEach(children::add);
    }

    public MaridRuntimeBean() {
        this(null, "beans", null, call(() -> signature(Object.class.getConstructor())));
    }

    @Nonnull
    @Override
    public MaridRuntimeBean add(@Nonnull String name,
                                @Nullable String factory,
                                @Nonnull String signature,
                                @Nonnull String... arguments) {
        final MaridRuntimeBean child = new MaridRuntimeBean(this, name, factory, signature, arguments);
        children.add(child);
        return child;
    }

    @Nonnull
    @Override
    public MaridRuntimeMethod add(@Nonnull String signature, @Nonnull String... arguments) {
        final MaridRuntimeMethod method = new MaridRuntimeMethod(this, signature, arguments);
        initializers.add(method);
        return method;
    }

    public MaridRuntimeBean add(@Nonnull String name,
                                @Nullable String factory,
                                @Nonnull Constructor<?> constructor,
                                @Nonnull String... arguments) {
        return add(name, factory, signature(constructor), arguments);
    }

    public MaridRuntimeBean add(@Nonnull String name,
                                @Nonnull Constructor<?> constructor,
                                @Nonnull String... arguments) {
        return add(name, null, constructor, arguments);
    }

    public MaridRuntimeBean add(@Nonnull String name,
                                @Nullable String factory,
                                @Nonnull Method method,
                                @Nonnull String... arguments) {
        return add(name, factory, signature(method), arguments);
    }

    public MaridRuntimeBean add(@Nonnull String name,
                                @Nonnull Method method,
                                @Nonnull String... arguments) {
        return add(name, null, method, arguments);
    }

    public MaridRuntimeBean add(@Nonnull String name,
                                @Nullable String factory,
                                @Nonnull Field field,
                                @Nonnull String... arguments) {
        return add(name, factory, signature(field), arguments);
    }

    public MaridRuntimeBean add(@Nonnull String name,
                                @Nonnull Field field,
                                @Nonnull String... arguments) {
        return add(name, null, field, arguments);
    }

    public MaridRuntimeMethod add(@Nonnull Constructor<?> constructor, @Nonnull String... arguments) {
        return add(signature(constructor), arguments);
    }

    public MaridRuntimeMethod add(@Nonnull Method method, @Nonnull String... arguments) {
        return add(signature(method), arguments);
    }

    public MaridRuntimeMethod add(@Nonnull Field field, @Nonnull String... arguments) {
        return add(signature(field), arguments);
    }

    @Nonnull
    @Override
    public String getName() {
        return name;
    }

    @Nullable
    @Override
    public String getFactory() {
        return factory;
    }

    @Nonnull
    @Override
    public List<MaridRuntimeMethod> getInitializers() {
        return initializers;
    }

    @Nonnull
    @Override
    public List<MaridRuntimeBean> getChildren() {
        return children;
    }

    @Override
    public void writeTo(Element element) {
        super.writeTo(element);

        element.setAttribute("name", name);

        if (factory != null) {
            element.setAttribute("factory", factory);
        }

        for (final MaridRuntimeMethod m : initializers) {
            m.writeTo(build(element.getOwnerDocument().createElement("initializer"), element::appendChild));
        }

        for (final MaridRuntimeBean b : children) {
            b.writeTo(build(element.getOwnerDocument().createElement("bean"), element::appendChild));
        }
    }

    public static MaridRuntimeBean load(MaridRuntimeBean parent, Reader reader) {
        return read(reader, e -> new MaridRuntimeBean(parent, e));
    }

    public void save(Writer writer) {
        writeFormatted("bean", this::writeTo, new StreamResult(writer));
    }

    @Override
    public String toString() {
        return factory == null
                ? name + initializers + "(" + super.toString() +")" + children
                : name + "(" + factory + ")" + initializers + "(" + super.toString() + ")" + children;
    }
}
