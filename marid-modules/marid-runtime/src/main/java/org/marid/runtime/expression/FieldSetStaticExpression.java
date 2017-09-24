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
import static org.marid.runtime.expression.FieldSetExpression.value;
import static org.marid.runtime.expression.MethodCallExpression.target;

public class FieldSetStaticExpression extends Expression {

    @Nonnull
    private final Expression target;

    @Nonnull
    private final String field;

    @Nonnull
    private final Expression value;

    public FieldSetStaticExpression(@Nonnull Expression target, @Nonnull String field, @Nonnull Expression value) {
        this.target = target;
        this.field = field;
        this.value = value;
    }

    public FieldSetStaticExpression(@Nonnull Element element) {
        field = attribute(element, "field").orElseThrow(() -> new NullPointerException("field"));
        target = target(element);
        value = value(element);
    }

    @Nonnull
    @Override
    public String getTag() {
        return "static-set";
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
        final Class<?> t = (Class<?>) requireNonNull(target.evaluate(self, context), "target");
        final Object v = value.evaluate(self, context);
        try {
            final Field f = t.getField(field);
            f.setAccessible(true);
            f.set(null, v);
            return self;
        } catch (NoSuchFieldException x) {
            throw new NoSuchElementException(field);
        } catch (IllegalAccessException x) {
            throw new IllegalStateException(x);
        }
    }
}
