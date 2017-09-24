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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.NoSuchElementException;

import static java.util.Objects.requireNonNull;
import static org.marid.io.Xmls.attribute;
import static org.marid.io.Xmls.elements;
import static org.marid.misc.Builder.build;

public class StaticFieldAccessExpression extends Expression {

    @Nonnull
    private final Expression target;

    @Nonnull
    private final String field;

    public StaticFieldAccessExpression(@Nonnull Expression target, @Nonnull String field) {
        this.target = target;
        this.field = field;
    }

    public StaticFieldAccessExpression(@Nonnull Element element) {
        target = elements(element).map(Expression::from).findFirst().orElseThrow(NoSuchElementException::new);
        field = attribute(element, "field").orElseThrow(() -> new NullPointerException("field"));
    }

    @Nonnull
    @Override
    public String getTag() {
        return "static-get";
    }

    @Override
    public void saveTo(@Nonnull Element element) {
        element.setAttribute("field", field);

        final Document document = element.getOwnerDocument();
        target.saveTo(build(document.createElement(target.getTag()), element::appendChild));
    }

    @Override
    protected Object execute(@Nullable Object self, @Nonnull BeanContext context) {
        final String field = context.resolvePlaceholders(this.field);
        final Class<?> t = (Class<?>) requireNonNull(target.evaluate(self, context), "target");
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

    @Override
    public String toString() {
        return target + "!" + field;
    }
}
