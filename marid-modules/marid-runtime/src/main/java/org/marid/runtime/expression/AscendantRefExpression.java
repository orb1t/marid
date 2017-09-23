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

public class AscendantRefExpression extends Expression {

    @Nonnull
    private final String reference;

    public AscendantRefExpression(@Nonnull String reference) {
        this.reference = reference;
    }

    public AscendantRefExpression(@Nonnull Element element) {
        this.reference = Xmls.attribute(element, "ref").orElseThrow(IllegalStateException::new);
    }

    @Nonnull
    public String getReference() {
        return reference;
    }

    @Nonnull
    @Override
    public String getTag() {
        return "ref-asc";
    }

    @Override
    public void saveTo(@Nonnull Element element) {
        element.setAttribute("ref", reference);
    }

    @Override
    public Object execute(@Nonnull BeanContext context) {
        return context.getAscendant(context.resolvePlaceholders(reference));
    }

    @Override
    public String toString() {
        return "@" + reference;
    }
}
