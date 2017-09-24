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

import org.marid.io.Xmls;
import org.marid.runtime.context2.BeanContext;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.NoSuchElementException;

public class ClassExpression extends Expression {

    @Nonnull
    private final String className;

    public ClassExpression(@Nonnull String className) {
        this.className = className;
    }

    public ClassExpression(@Nonnull Element element) {
        className = Xmls.attribute(element, "class").orElseThrow(() -> new NullPointerException("class"));
    }

    @Nonnull
    @Override
    public String getTag() {
        return "class";
    }

    @Override
    public void saveTo(@Nonnull Element element) {
        element.setAttribute("class", className);
    }

    @Override
    protected Object execute(@Nullable Object self, @Nonnull BeanContext context) {
        final String className = context.resolvePlaceholders(this.className);
        try {
            return context.getClassLoader().loadClass(className);
        } catch (ClassNotFoundException x) {
            throw new NoSuchElementException(className);
        }
    }

    @Override
    public String toString() {
        return "&" + className;
    }
}
