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
import java.lang.reflect.Field;
import java.util.NoSuchElementException;

import static java.util.Objects.requireNonNull;
import static org.marid.io.Xmls.attribute;
import static org.marid.io.Xmls.elements;

public class FieldAccessExpression extends Expression {

    @Nonnull
    private final Expression target;

    @Nonnull
    private final String field;

    public FieldAccessExpression(@Nonnull Expression target, @Nonnull String field) {
        this.target = target;
        this.field = field;
    }

    public FieldAccessExpression(@Nonnull Element element) {
        target = elements(element).map(Expression::from).findFirst().orElseThrow(NoSuchElementException::new);
        field = attribute(element, "field").orElseThrow(() -> new NullPointerException("field"));
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
    @Override
    public String getTag() {
        return "get";
    }

    @Override
    public void saveTo(@Nonnull Element element) {
        final Document document = element.getOwnerDocument();
        final Element t = document.createElement(target.getTag());
        element.appendChild(t);
        target.saveTo(t);

        element.setAttribute("field", field);
    }

    @Override
    public Object execute(@Nonnull BeanContext context) {
        final String field = context.resolvePlaceholders(this.field);
        final Object t = requireNonNull(target.execute(context), "target");
        final Class<?> c = target instanceof ClassExpression ? (Class<?>) t : t.getClass();
        try {
            final Field f = c.getField(field);
            f.setAccessible(true);
            return f.get(t);
        } catch (NoSuchFieldException x) {
            throw new NoSuchElementException(field);
        } catch (IllegalAccessException x) {
            throw new IllegalStateException(x);
        }
    }

    @Override
    public String toString() {
        return target + "." + field;
    }
}
