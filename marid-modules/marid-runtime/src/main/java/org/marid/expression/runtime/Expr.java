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

package org.marid.expression.runtime;

import org.marid.expression.generic.Expression;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.marid.io.Xmls.create;
import static org.marid.io.Xmls.elements;

public abstract class Expr implements Expression {

    private final List<Expr> initializers;

    public Expr(@Nonnull Element element) {
        initializers = elements("initializers", element).map(Expr::of).collect(toList());
    }

    public Expr() {
        initializers = new ArrayList<>();
    }

    @Nonnull
    @Override
    public List<Expr> getInitializers() {
        return initializers;
    }

    public String getTag() {
        return getClass().getSimpleName().replace("Expr", "").toLowerCase();
    }

    public void writeTo(@Nonnull Element element) {
        create(element, "initializers", is -> initializers.forEach(i -> create(is, i.getTag(), i::writeTo)));
    }

    public static Expr of(@Nonnull Element element) {
        switch (element.getTagName()) {
            case "class": return new ClassExpr(element);
            case "this": return new ThisExpr(element);
            case "string": return new StringExpr(element);
            case "ref": return new RefExpr(element);
            case "const": return new ConstExpr(element);
            case "get": return new GetExpr(element);
            case "set": return new SetExpr(element);
            case "null": return new NullExpr(element);
            case "call": return new CallExpr(element);
            default: throw new IllegalArgumentException(element.getTagName());
        }
    }
}
