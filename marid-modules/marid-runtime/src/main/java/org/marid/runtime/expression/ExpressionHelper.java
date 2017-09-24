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

import java.util.Collections;

public interface ExpressionHelper {

    NullExpression NULL = NullExpression.NULL;
    ThisExpression THIS = ThisExpression.THIS;

    static ParentExpression $(int level) {
        return new ParentExpression(level);
    }

    static RefExpression $r(String ref) {
        return new RefExpression(ref);
    }

    static StaticMethodCallExpression $(Class<?> type, String name, Expression... args) {
        return new StaticMethodCallExpression(new ClassExpression(type.getName()), name, args);
    }

    static MethodCallExpression $(Expression target, String name, Expression... args) {
        return new MethodCallExpression(target, name, args);
    }

    static StaticFieldAccessExpression $(String name, Class<?> type) {
        return new StaticFieldAccessExpression(new ClassExpression(type.getName()), name);
    }

    static FieldAccessExpression $(String name, Expression target) {
        return new FieldAccessExpression(target, name);
    }

    static ConstructorCallExpression $(Expression target, Expression... args) {
        return new ConstructorCallExpression(target, args);
    }

    static ConstructorCallExpression $(Class<?> target, Expression... args) {
        return new ConstructorCallExpression(new ClassExpression(target.getName()), args);
    }

    static StringExpression $s(String value) {
        return new StringExpression(value);
    }

    static IntegerExpression $i(String value) {
        return new IntegerExpression(value);
    }

    static LongExpression $l(String value) {
        return new LongExpression(value);
    }

    static <T extends Expression> T $init(T expression, Expression... initializers) {
        Collections.addAll(expression.getInitializers(), initializers);
        return expression;
    }
}
