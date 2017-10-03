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
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.marid.io.Xmls.attribute;

public class RefExpr extends AbstractExpression implements RefExpression {

    @Nonnull
    private String reference;

    public RefExpr(@Nonnull String reference) {
        this.reference = reference;
    }

    public RefExpr() {
        reference = "";
    }

    @Override
    @Nonnull
    public String getReference() {
        return reference;
    }

    @Override
    public void saveTo(@Nonnull Element element) {
        element.setAttribute("ref", reference);
    }

    @Override
    public void loadFrom(@Nonnull Element element) {
        reference = attribute(element, "ref").orElseThrow(IllegalStateException::new);
    }

    @Override
    protected Object execute(@Nullable Object self, @Nonnull BeanContext context) {
        return context.getBean(context.resolvePlaceholders(reference));
    }

    @Override
    public String toString() {
        return "@" + reference;
    }
}
