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

import org.jetbrains.annotations.NotNull;
import org.marid.io.Xmls;
import org.marid.runtime.context2.BeanContext;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class ValueExpression extends Expression {

    @Nonnull
    private final String value;

    public ValueExpression(@Nonnull String value) {
        this.value = value;
    }

    public ValueExpression(@Nonnull Element element) {
        value = Xmls.attribute(element, "value").orElseThrow(() -> new NullPointerException("value"));
    }

    @Override
    public void saveTo(@NotNull Element element) {
        element.setAttribute("value", value);
    }

    @Nonnull
    public String getValue() {
        return value;
    }

    @Override
    protected Object execute(@Nullable Object self, @Nonnull BeanContext context) {
        return parseSubstituted(context.resolvePlaceholders(value));
    }

    protected abstract Object parseSubstituted(@Nonnull String substituted);

    @Override
    public String toString() {
        return value;
    }
}
