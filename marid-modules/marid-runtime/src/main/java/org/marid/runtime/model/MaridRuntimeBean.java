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

import org.marid.runtime.expression.Expression;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.transform.stream.StreamResult;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import static java.util.stream.Collectors.toList;
import static org.marid.io.Xmls.*;
import static org.marid.misc.Builder.build;
import static org.marid.runtime.expression.NullExpression.NULL;

public class MaridRuntimeBean implements MaridBean {

    private final MaridRuntimeBean parent;
    private final String name;
    private final Expression factory;
    private final List<Expression> initializers;
    private final List<MaridRuntimeBean> children;

    public MaridRuntimeBean(@Nullable MaridRuntimeBean parent,
                            @Nonnull String name,
                            @Nonnull Expression factory,
                            @Nonnull Expression... initializers) {
        this.parent = parent;
        this.name = name;
        this.factory = factory;
        this.initializers = Arrays.asList(initializers);
        this.children = new ArrayList<>();
    }

    public MaridRuntimeBean(MaridRuntimeBean parent, Element element) {
        this.parent = parent;
        this.name = attribute(element, "name").orElseThrow(() -> new NullPointerException("name"));
        this.factory = elements(element)
                .filter(e -> "factory".equals(e.getTagName()))
                .flatMap(e -> elements(e).map(Expression::from))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("factory"));
        this.initializers = elements(element)
                .filter(e -> "initializer".equals(e.getTagName()))
                .map(e -> elements(e).map(Expression::from).findFirst().orElseThrow(NoSuchElementException::new))
                .collect(toList());
        this.children = elements(element)
                .filter(e -> "bean".equals(e.getTagName()))
                .map(e -> new MaridRuntimeBean(this, e))
                .collect(toList());

    }

    public MaridRuntimeBean() {
        this(null, "beans", NULL);
    }

    @Nullable
    @Override
    public MaridRuntimeBean getParent() {
        return parent;
    }

    @Nonnull
    @Override
    public String getName() {
        return name;
    }

    @Nonnull
    @Override
    public Expression getFactory() {
        return factory;
    }

    @Nonnull
    @Override
    public List<Expression> getInitializers() {
        return initializers;
    }

    @Nonnull
    @Override
    public List<MaridRuntimeBean> getChildren() {
        return children;
    }

    @Nonnull
    @Override
    public MaridRuntimeBean add(@Nonnull String name, @Nonnull Expression factory, @Nonnull Expression... initializers) {
        final MaridRuntimeBean bean = new MaridRuntimeBean(this, name, factory, initializers);
        children.add(bean);
        return bean;
    }

    public void writeTo(Element element) {
        element.setAttribute("name", name);

        final Document document = element.getOwnerDocument();

        final Element factoryElement = build(document.createElement("factory"), element::appendChild);
        factory.saveTo(build(document.createElement(factory.getTag()), factoryElement::appendChild));

        for (final Expression initializer : initializers) {
            final Element ee = build(document.createElement(initializer.getTag()), initializer::saveTo);
            element.appendChild(build(document.createElement("initializer"), i -> i.appendChild(ee)));
        }

        children.forEach(b -> b.writeTo(build(document.createElement("bean"), element::appendChild)));
    }

    public static MaridRuntimeBean load(MaridRuntimeBean parent, Reader reader) {
        return read(reader, e -> new MaridRuntimeBean(parent, e));
    }

    public void save(Writer writer) {
        writeFormatted("bean", this::writeTo, new StreamResult(writer));
    }

    @Override
    public String toString() {
        return name + "(" + factory + initializers + children + ")";
    }
}
