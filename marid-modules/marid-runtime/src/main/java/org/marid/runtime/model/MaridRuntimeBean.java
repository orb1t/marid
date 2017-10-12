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
import javax.xml.transform.stream.StreamResult;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;
import static org.marid.io.Xmls.*;
import static org.marid.misc.Builder.build;
import static org.marid.runtime.expression.NullExpr.NULL;

public class MaridRuntimeBean implements MaridBean {

    private final MaridRuntimeBean parent;
    private final String name;
    private final Expression factory;
    private final List<MaridRuntimeBean> children;

    public MaridRuntimeBean(MaridRuntimeBean parent, @Nonnull String name, @Nonnull Expression factory) {
        this.parent = parent;
        this.name = name;
        this.factory = factory;
        this.children = new ArrayList<>();
    }

    public MaridRuntimeBean(MaridRuntimeBean parent, Element element) {
        this.parent = parent;
        this.name = attribute(element, "name").orElseThrow(() -> new NullPointerException("name"));
        this.factory = elements(element)
                .filter(e -> "factory".equals(e.getTagName()))
                .flatMap(e -> elements(e).map(NULL::from))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("factory"));
        this.children = elements(element)
                .filter(e -> "bean".equals(e.getTagName()))
                .map(e -> new MaridRuntimeBean(this, e))
                .collect(toList());

    }

    public MaridRuntimeBean() {
        this(null, "beans", NULL);
    }

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
    public List<MaridRuntimeBean> getChildren() {
        return children;
    }

    @SafeVarargs
    @Nonnull
    public final MaridRuntimeBean add(@Nonnull String name,
                                      @Nonnull Expression factory,
                                      @Nonnull Consumer<MaridRuntimeBean>... consumers) {
        final MaridRuntimeBean bean = new MaridRuntimeBean(this, name, factory);
        children.add(bean);
        for (final Consumer<MaridRuntimeBean> consumer : consumers) {
            consumer.accept(bean);
        }
        return this;
    }

    public void writeTo(Element element) {
        element.setAttribute("name", name);

        final Document document = element.getOwnerDocument();

        final Element factoryElement = build(document.createElement("factory"), element::appendChild);
        factory.to(factoryElement);

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
        return name + "(" + factory + children + ")";
    }
}
