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

import org.marid.runtime.context2.BeanContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.NoSuchElementException;

import static java.util.Objects.requireNonNull;
import static org.marid.io.Xmls.attribute;
import static org.marid.io.Xmls.elements;
import static org.marid.misc.Builder.build;
import static org.marid.runtime.expression.MethodCallExpression.target;

public class FieldSetExpression extends Expression {

    @Nonnull
    private final Expression target;

    @Nonnull
    private final String field;

    @Nonnull
    private final Expression value;

    public FieldSetExpression(@Nonnull Expression target, @Nonnull String field, @Nonnull Expression value) {
        this.target = target;
        this.field = field;
        this.value = value;
    }

    public FieldSetExpression(@Nonnull Element element) {
        field = attribute(element, "field").orElseThrow(() -> new NullPointerException("field"));
        target = target(element);
        value = value(element);
    }

    @Nonnull
    public Expression getTarget() {
        return target;
    }

    @Nonnull
    public String getField() {
        return field;
    }

    @Nonnull
    public Expression getValue() {
        return value;
    }

    @Nonnull
    @Override
    public String getTag() {
        return "set";
    }

    @Override
    public void saveTo(@Nonnull Element element) {
        element.setAttribute("field", field);
        target(element, target);
        value(element, value);
    }

    @Override
    protected Object execute(@Nullable Object self, @Nonnull BeanContext context) {
        final String field = context.resolvePlaceholders(this.field);
        final Object t = requireNonNull(target.evaluate(self, context), "target");
        final Object v = value.evaluate(self, context);
        try {
            final Field f = t.getClass().getField(field);
            f.setAccessible(true);
            f.set(t, v);
            return t;
        } catch (NoSuchFieldException x) {
            throw new NoSuchElementException(field);
        } catch (IllegalAccessException x) {
            throw new IllegalStateException(x);
        }
    }

    public static Expression value(Element element) {
        return elements(element)
                .filter(e -> "value".equals(e.getTagName()))
                .flatMap(e -> elements(e).map(Expression::from))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("value"));
    }

    public static void value(Element element, Expression target) {
        final Document document = element.getOwnerDocument();
        final Element targetElement = build(document.createElement("value"), element::appendChild);
        target.saveTo(build(document.createElement(target.getTag()), targetElement::appendChild));
    }
}
