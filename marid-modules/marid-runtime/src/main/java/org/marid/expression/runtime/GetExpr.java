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

import org.marid.expression.generic.GetExpression;
import org.marid.runtime.context.BeanContext;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.NoSuchElementException;

import static org.marid.io.Xmls.attribute;
import static org.marid.io.Xmls.element;

public final class GetExpr extends Expr implements GetExpression {

    @Nonnull
    private final Expr target;

    @Nonnull
    private final String field;

    public GetExpr(@Nonnull Expr target, @Nonnull String field) {
        this.target = target;
        this.field = field;
    }

    GetExpr(@Nonnull Element element) {
        super(element);
        this.target = element("target", element).map(Expr::of).orElseThrow(() -> new NullPointerException("target"));
        this.field = attribute(element, "field").orElseThrow(() -> new NullPointerException("field"));
    }

    @Override
    @Nonnull
    public Class<?> getType(@Nonnull BeanContext context, @Nullable Class<?> self) {
        final Class<?> target = getTarget().targetType(context, self);
        try {
            return target.getField(getField()).getType();
        } catch (NoSuchFieldException x) {
            throw new NoSuchElementException(getField());
        }
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
            if (Modifier.isStatic(field.getModifiers())) {
                return field.get(null);
            } else {
                return field.get(getTarget().evaluate(self, selfType, context));
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
    public String toString() {
        return target + "." + field;
    }
}
