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
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.NoSuchElementException;

import static java.util.Objects.requireNonNull;
import static org.marid.io.Xmls.attribute;
import static org.marid.runtime.expression.MethodCallExpression.target;

public class FieldGetExpression extends Expression {

    @Nonnull
    private final Expression target;

    @Nonnull
    private final String field;

    public FieldGetExpression(@Nonnull Expression target, @Nonnull String field) {
        this.target = target;
        this.field = field;
    }

    public FieldGetExpression(@Nonnull Element element) {
        target = target(element);
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
        element.setAttribute("field", field);
        target(element, target);
    }

    @Override
    protected Object execute(@Nullable Object self, @Nonnull BeanContext context) {
        final String field = context.resolvePlaceholders(this.field);
        final Object t = requireNonNull(target.evaluate(self, context), "target");
        try {
            final Field f = t.getClass().getField(field);
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
