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
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.List;

public interface Expression {

    void saveTo(@Nonnull Element element);

    void loadFrom(@Nonnull Element element);

    @Nullable
    Object evaluate(@Nullable Object self, @Nonnull BeanContext context);

    @Nonnull
    Type getType(@Nullable Type owner, @Nonnull TypeContext typeContext);

    @Nonnull
    List<? extends Expression> getInitializers();

    @Nonnull
    default Element newElement(@Nonnull Element element) {
        final String tag = getClass().getSimpleName().replace("Expr", "");
        return element.getOwnerDocument().createElement(tag);
    }

    @Nonnull
    default Expression newInstanceFrom(@Nonnull Element element) {
        final Package p = getClass().getPackage();
        final ClassLoader classLoader = getClass().getClassLoader();
        final String className = p.getName() + "." + element.getTagName() + "Expr";
        try {
            return (Expression) classLoader.loadClass(className).getConstructor().newInstance();
        } catch (ReflectiveOperationException x) {
            throw new IllegalStateException(x);
        }
    }

    @Nonnull
    default Expression from(@Nonnull Element element) {
        final Expression expression = newInstanceFrom(element);
        expression.loadFrom(element);
        return expression;
    }

    default void to(@Nonnull Element element) {
        final Element e = newElement(element);
        saveTo(e);
        element.appendChild(e);
    }
}
