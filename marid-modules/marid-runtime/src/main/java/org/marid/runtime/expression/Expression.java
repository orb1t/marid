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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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
    default Expression from(@Nonnull Element element) {
        final Package p = getClass().getPackage();
        final ClassLoader classLoader = getClass().getClassLoader();
        final String className = p.getName() + "." + element.getTagName() + "Expr";
        try {
            final Class<?> clazz = classLoader.loadClass(className);
            final Optional<Field> field = Stream.of(clazz.getFields())
                    .filter(f -> Modifier.isStatic(f.getModifiers()))
                    .filter(f -> Modifier.isFinal(f.getModifiers()))
                    .filter(f -> Expression.class.isAssignableFrom(f.getType()))
                    .findFirst();
            if (field.isPresent()) {
                return (Expression) field.get().get(null);
            } else {
                final Expression expression = (Expression) clazz.getConstructor().newInstance();
                expression.loadFrom(element);
                return expression;
            }
        } catch (ReflectiveOperationException x) {
            throw new IllegalStateException(x);
        }
    }

    default void to(@Nonnull Node element) {
        final String tag = getClass().getSimpleName().replace("Expr", "");
        final Element e;
        if (element instanceof Document) {
            e = ((Document) element).createElement(tag);
        } else {
            e = element.getOwnerDocument().createElement(tag);
        }
        saveTo(e);
        element.appendChild(e);
    }
}
