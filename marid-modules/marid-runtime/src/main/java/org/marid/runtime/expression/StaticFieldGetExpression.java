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

import org.marid.runtime.context.BeanContext;
import org.marid.runtime.types.TypeContext;
import org.marid.runtime.util.ReflectUtils;
import org.marid.runtime.util.TypeUtils;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.NoSuchElementException;

import static java.util.Objects.requireNonNull;
import static org.marid.io.Xmls.attribute;
import static org.marid.runtime.expression.MethodCallExpr.target;

public interface StaticFieldGetExpression extends Expression {

    @Nonnull
    Expression getTarget();

    void setTarget(@Nonnull Expression target);

    @Nonnull
    String getField();

    void setField(@Nonnull String field);

    @Override
    default void saveTo(@Nonnull Element element) {
        element.setAttribute("field", getField());
        target(element, getTarget());
    }

    @Override
    default void loadFrom(@Nonnull Element element) {
        setTarget(target(element, this::from));
        setField(attribute(element, "field").orElseThrow(() -> new NullPointerException("field")));
    }

    @Nonnull
    @Override
    default Type getType(@Nullable Type owner, @Nonnull TypeContext typeContext) {
        final Type targetType = getTarget().getType(owner, typeContext);
        final String field = typeContext.resolvePlaceholders(getField());
        return TypeUtils.classType(targetType)
                .flatMap(tc -> TypeUtils.getField(typeContext.getRaw(tc), field)
                        .map(f -> typeContext.resolve(owner, f.getGenericType())))
                .orElseGet(typeContext::getWildcard);
    }

    @Nullable
    @Override
    default Object evaluate(@Nullable Object self, @Nonnull BeanContext context) {
        return ReflectUtils.evaluate(this::execute, this).apply(self, context);
    }

    private Object execute(@Nullable Object self, @Nonnull BeanContext context) {
        final String field = context.resolvePlaceholders(getField());
        final Class<?> t = (Class<?>) requireNonNull(getTarget().evaluate(self, context), "target");
        try {
            final Field f = t.getField(field);
            f.setAccessible(true);
            return f.get(null);
        } catch (NoSuchFieldException x) {
            throw new NoSuchElementException(field);
        } catch (IllegalAccessException x) {
            throw new IllegalStateException(x);
        }
    }
}
