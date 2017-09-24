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
import java.util.ArrayList;
import java.util.List;

import static org.marid.runtime.expression.NullExpression.NULL;
import static org.marid.runtime.expression.ThisExpression.THIS;

public abstract class Expression {

    private final List<Expression> initializers = new ArrayList<>();

    @Nonnull
    public abstract String getTag();

    public abstract void saveTo(@Nonnull Element element);

    protected abstract Object execute(@Nullable Object self, @Nonnull BeanContext context);

    public Object evaluate(@Nullable Object self, @Nonnull BeanContext context) {
        final Object v = execute(self, context);
        initializers.forEach(i -> i.evaluate(v, context));
        return v;
    }

    public List<Expression> getInitializers() {
        return initializers;
    }

    @Nonnull
    public static Expression from(Element element) {
        switch (element.getTagName()) {
            case "null": return NULL;
            case "this": return THIS;
            case "parent": return new ParentExpression(element);
            case "int": return new IntegerExpression(element);
            case "long": return new LongExpression(element);
            case "char": return new CharExpression(element);
            case "short": return new ShortExpression(element);
            case "double": return new DoubleExpression(element);
            case "float": return new FloatExpression(element);
            case "byte": return new ByteExpression(element);
            case "string": return new StringExpression(element);
            case "boolean": return new BooleanExpression(element);
            case "ref": return new RefExpression(element);
            case "call": return new MethodCallExpression(element);
            case "static-call": return new MethodCallStaticExpression(element);
            case "new": return new ConstructorCallExpression(element);
            case "set": return new FieldSetExpression(element);
            case "static-set": return new FieldSetStaticExpression(element);
            case "get": return new FieldGetExpression(element);
            case "static-get": return new FieldGetStaticExpression(element);
            case "apply": return new ApplyExpression(element);
            case "class": return new ClassExpression(element);
            default: throw new UnsupportedOperationException("Unknown expression type: " + element.getTagName());
        }
    }

    public static void save(@Nonnull Element element, @Nonnull Expression expression) {
        final Document document = element.getOwnerDocument();
        final Element child = document.createElement(expression.getTag());
        expression.saveTo(child);
        element.appendChild(element);
    }
}
