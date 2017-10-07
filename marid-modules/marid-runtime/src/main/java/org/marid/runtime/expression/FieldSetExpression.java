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
import org.marid.runtime.types.TypeContext;
import org.marid.runtime.util.ReflectUtils;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.NoSuchElementException;

import static java.util.Objects.requireNonNull;
import static org.marid.io.Xmls.attribute;
import static org.marid.runtime.expression.FieldSetExpr.value;
import static org.marid.runtime.expression.MethodCallExpr.target;
import static org.marid.runtime.expression.NullExpr.NULL;

public interface FieldSetExpression extends Expression {

    @Nonnull
    Expression getTarget();

    void setTarget(@Nonnull Expression target);

    @Nonnull
    String getField();

    void setField(@Nonnull String field);

    @Nonnull
    Expression getValue();

    void setValue(@Nonnull Expression value);

    @Override
    default void saveTo(@Nonnull Element element) {
        element.setAttribute("field", getField());
        target(element, getTarget());
        value(element, getValue());
    }

    @Override
    default void loadFrom(@Nonnull Element element) {
        setField(attribute(element, "field").orElseThrow(() -> new NullPointerException("field")));
        setTarget(target(element, NULL::from));
        setValue(value(element, NULL::from));
    }

    @Nonnull
    @Override
    default Type getType(@Nullable Type owner, @Nonnull TypeContext typeContext) {
        return getTarget().getType(owner, typeContext);
    }

    @Nullable
    @Override
    default Object evaluate(@Nullable Object self, @Nonnull BeanContext context) {
        return ReflectUtils.evaluate(this::execute, this).apply(self, context);
    }

    private Object execute(@Nullable Object self, @Nonnull BeanContext context) {
        final String field = context.resolvePlaceholders(getField());
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