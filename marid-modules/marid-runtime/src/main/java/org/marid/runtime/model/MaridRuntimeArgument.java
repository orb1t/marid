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

package org.marid.runtime.model;

import org.marid.runtime.expression.Expression;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;

public class MaridRuntimeArgument implements MaridArgument {

    private final MaridRuntimeMethod parent;
    private final Expression expression;

    public MaridRuntimeArgument(@Nonnull MaridRuntimeMethod parent, @Nonnull Expression expression) {
        this.parent = parent;
        this.expression = expression;
    }

    @Nonnull
    @Override
    public MaridMethod getParent() {
        return parent;
    }

    @Nonnull
    @Override
    public Expression getExpression() {
        return expression;
    }

    public void writeTo(Element element) {
        expression.saveTo(element);
    }

    @Override
    public String toString() {
        return expression.toString();
    }
}
