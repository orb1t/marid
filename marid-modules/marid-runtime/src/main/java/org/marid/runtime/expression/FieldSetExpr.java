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

package org.marid.runtime.expression;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import java.util.NoSuchElementException;
import java.util.function.Function;

import static org.marid.io.Xmls.attribute;
import static org.marid.io.Xmls.elements;
import static org.marid.misc.Builder.build;
import static org.marid.runtime.expression.MethodCallExpr.target;
import static org.marid.runtime.expression.NullExpr.NULL;

public class FieldSetExpr extends AbstractExpression implements FieldSetExpression {

    @Nonnull
    private Expression target;

    @Nonnull
    private String field;

    @Nonnull
    private Expression value;

    public FieldSetExpr(@Nonnull Expression target, @Nonnull String field, @Nonnull Expression value) {
        this.target = target;
        this.field = field;
        this.value = value;
    }

    public FieldSetExpr() {
        target = NULL;
        field = "";
        value = NULL;
    }

    @Override
    @Nonnull
    public Expression getTarget() {
        return target;
    }

    @Override
    public void setTarget(@Nonnull Expression target) {
        this.target = target;
    }

    @Override
    @Nonnull
    public String getField() {
        return field;
    }

    @Override
    public void setField(@Nonnull String field) {
        this.field = field;
    }

    @Override
    @Nonnull
    public Expression getValue() {
        return value;
    }

    @Override
    public void setValue(@Nonnull Expression value) {
        this.value = value;
    }

    @Override
    public void saveTo(@Nonnull Element element) {
        element.setAttribute("field", field);
        target(element, target);
        value(element, value);
    }

    @Override
    public void loadFrom(@Nonnull Element element) {
        field = attribute(element, "field").orElseThrow(() -> new NullPointerException("field"));
        target = target(element, NULL::from);
        value = value(element, NULL::from);
    }

    public static Expression value(Element element, Function<Element, Expression> expressionFactory) {
        return elements(element)
                .filter(e -> "value".equals(e.getTagName()))
                .flatMap(e -> elements(e).map(expressionFactory))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("value"));
    }

    public static void value(Element element, Expression target) {
        final Document document = element.getOwnerDocument();
        final Element valueElement = build(document.createElement("value"), element::appendChild);
        target.to(valueElement);
    }
}
