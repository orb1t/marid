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

package org.marid.beans;

import org.marid.expression.generic.Expression;
import org.marid.expression.runtime.Expr;
import org.marid.expression.runtime.NullExpr;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.xml.transform.stream.StreamResult;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;
import static org.marid.io.Xmls.*;

public class RuntimeBean implements MaridBean {

    private final RuntimeBean parent;
    private final String name;
    private final Expr factory;
    private final List<RuntimeBean> children;

    public RuntimeBean(RuntimeBean parent, @Nonnull String name, @Nonnull Expr factory) {
        this.parent = parent;
        this.name = name;
        this.factory = factory;
        this.children = new ArrayList<>();
    }

    public RuntimeBean(RuntimeBean parent, @Nonnull Element element) {
        this.parent = parent;
        this.name = attribute(element, "name").orElseThrow(() -> new NullPointerException("name"));
        this.factory = element("factory", element).map(Expr::of).orElseThrow(() -> new NullPointerException("factory"));
        this.children = elements(element, "bean").map(e -> new RuntimeBean(this, e)).collect(toList());

    }

    public RuntimeBean() {
        this(null, "beans", new NullExpr());
    }

    @Override
    public RuntimeBean getParent() {
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
    public List<RuntimeBean> getChildren() {
        return children;
    }

    @SafeVarargs
    @Nonnull
    public final RuntimeBean add(@Nonnull String name, @Nonnull Expr factory, @Nonnull Consumer<RuntimeBean>... consumers) {
        final RuntimeBean bean = new RuntimeBean(this, name, factory);
        children.add(bean);
        for (final Consumer<RuntimeBean> consumer : consumers) {
            consumer.accept(bean);
        }
        return this;
    }

    @SafeVarargs
    @Nonnull
    public final RuntimeBean add(@Nonnull String name, @Nonnull Consumer<RuntimeBean>... consumers) {
        return add(name, new NullExpr(), consumers);
    }

    public void writeTo(@Nonnull Element element) {
        element.setAttribute("name", name);
        create(element, "factory", f -> create(f, factory.getTag(), factory::writeTo));
        children.forEach(c -> create(element, "bean", c::writeTo));
    }

    public void save(Writer writer) {
        writeFormatted("bean", this::writeTo, new StreamResult(writer));
    }

    @Override
    public String toString() {
        if (children.isEmpty()) {
            return name + "(" + factory + ")";
        } else {
            return name + "(" + factory + ")" + children;
        }
    }
}
