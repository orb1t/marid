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
import java.lang.reflect.Type;

import static org.marid.io.Xmls.attribute;

public interface RefExpression extends Expression {

    @Nonnull
    String getReference();

    void setReference(@Nonnull String reference);

    @Override
    default void saveTo(@Nonnull Element element) {
        element.setAttribute("ref", getReference());
    }

    @Override
    default void loadFrom(@Nonnull Element element) {
        setReference(attribute(element, "ref").orElseThrow(IllegalStateException::new));
    }

    @Nonnull
    @Override
    default Type getType(@Nullable Type owner, @Nonnull TypeContext typeContext) {
        return typeContext.getBeanType(typeContext.resolvePlaceholders(getReference()));
    }

    @Nullable
    @Override
    default Object evaluate(@Nullable Object self, @Nonnull BeanContext context) {
        return ReflectUtils.evaluate(this::execute, this).apply(self, context);
    }

    private Object execute(@Nullable Object self, @Nonnull BeanContext context) {
        return context.getBean(context.resolvePlaceholders(getReference()));
    }
}
