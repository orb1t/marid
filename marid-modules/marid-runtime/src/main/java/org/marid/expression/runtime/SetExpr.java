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

import org.marid.expression.generic.ClassExpression;
import org.marid.expression.generic.SetExpression;
import org.marid.runtime.context.BeanContext;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.marid.io.Xmls.attribute;
import static org.marid.io.Xmls.element;

public final class SetExpr extends Expr implements SetExpression {

    @Nonnull
    private Expr target;

    @Nonnull
    private String field;

    @Nonnull
    private Expr value;

    public SetExpr(@Nonnull Expr target, @Nonnull String field, @Nonnull Expr value) {
        this.target = target;
        this.field = field;
        this.value = value;
    }

    SetExpr(@Nonnull Element element) {
        super(element);
        target = element("target", element).map(Expr::of).orElseThrow(() -> new NullPointerException("target"));
        field = attribute(element, "field").orElseThrow(() -> new NullPointerException("field"));
        value = element("value", element).map(Expr::of).orElseThrow(() -> new NullPointerException("value"));
    }

    @Override
    protected Object execute(@Nullable Object self, @Nonnull BeanContext context) {
        final String field = context.resolvePlaceholders(getField());
        if (getTarget() instanceof ClassExpression) {
            final Class<?> t = (Class<?>) requireNonNull(getTarget().evaluate(self, context), "target");
            final Field f = Stream.of(t.getFields())
                    .filter(m -> field.equals(m.getName()))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException(field));
            try {
                f.setAccessible(true);
                f.set(null, getValue().evaluate(self, context));
                return null;
            } catch (IllegalAccessException x) {
                throw new IllegalStateException(x);
            }
        } else {
            final Object t = requireNonNull(getTarget().evaluate(self, context), "target");
            final Object v = getValue().evaluate(self, context);
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
    }

    @Override
    @Nonnull
    public Expr getTarget() {
        return target;
    }

    @Override
    @Nonnull
    public String getField() {
        return field;
    }

    @Override
    @Nonnull
    public Expr getValue() {
        return value;
    }
}
