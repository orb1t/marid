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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;

public abstract class Expression {

    protected Expression parent;

    public abstract String getTag();

    public abstract void saveTo(Element element);

    @Nonnull
    public static Expression from(Element element) {
        switch (element.getTagName()) {
            case "null":
                return NullExpression.NULL;
            case "int":
                return new IntegerExpression(element);
            case "long":
                return new LongExpression(element);
            case "string":
                return new StringExpression(element);
            case "asc-ref":
                return new AscendantRefExpression(element);
            case "desc-ref":
                return new DescendantRefExpression(element);
            case "call":
                return new MethodCallExpression(element);
            case "get":
                return new FieldAccessExpression(element);
            case "apply":
                return new ApplyExpression(element);
            default:
                throw new UnsupportedOperationException("Unknown expression type: " + element.getTagName());
        }
    }

    public static void save(Element element, Expression expression) {
        final Document document = element.getOwnerDocument();
        final Element child = document.createElement(expression.getTag());
        expression.saveTo(child);
        element.appendChild(element);
    }
}
