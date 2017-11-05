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

import org.marid.expression.generic.SetExpression;
import org.marid.runtime.context.BeanContext;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.NoSuchElementException;

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

    @Nonnull
    @Override
    public Class<?> getType(@Nonnull BeanContext context, @Nullable Class<?> self) {
        return getTarget().targetType(context, self);
    }

    @Override
    protected Object execute(@Nullable Object self, @Nullable Class<?> selfType, @Nonnull BeanContext context) {
        final Class<?> target = getTarget().targetType(context, selfType);
        final Field field;
        try {
            field = target.getField(getField());
        } catch (NoSuchFieldException x) {
            throw new NoSuchElementException(getField());
        }
        try {
            final Object v = getValue().evaluate(self, selfType, context);
            if (Modifier.isStatic(field.getModifiers())) {
                field.set(null, v);
                return target;
            } else {
                final Object t = getTarget().evaluate(self, selfType, context);
                field.set(t, v);
                return t;
            }
        } catch (IllegalAccessException x) {
            throw new IllegalStateException(x);
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
