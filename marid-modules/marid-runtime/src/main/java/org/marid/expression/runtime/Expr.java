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

package org.marid.expression.runtime;

import org.marid.expression.generic.Expression;
import org.marid.expression.xml.XmlExpression;
import org.marid.function.ToImmutableList;
import org.marid.runtime.context.BeanContext;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

public abstract class Expr implements Expression {

  private final List<Expr> initializers;

  public Expr(@Nonnull Element element) {
    initializers = XmlExpression.initializers(element, Expr::of, new ToImmutableList<>());
  }

  public Expr() {
    initializers = new LinkedList<>();
  }

  @Nonnull
  @Override
  public List<Expr> getInitializers() {
    return initializers;
  }

  @Nullable
  public final Object evaluate(@Nullable Object self, @Nullable Type selfType, @Nonnull BeanContext context) {
    final Object v = execute(self, selfType, context);
    final Type newSelf = getType(selfType, context);
    for (final Expr initializer : getInitializers()) {
      initializer.evaluate(v, newSelf, context);
    }
    return v;
  }

  protected abstract Object execute(@Nullable Object self, @Nullable Type selfType, @Nonnull BeanContext context);

  public static Expr of(@Nonnull Element element) {
    switch (element.getTagName()) {
      case "array": return new ArrayExpr(element);
      case "call": return new CallExpr(element);
      case "class": return new ClassExpr(element);
      case "this": return new ThisExpr(element);
      case "string": return new StringExpr(element);
      case "ref": return new RefExpr(element);
      case "get": return new GetExpr(element);
      case "set": return new SetExpr(element);
      case "null": return new NullExpr(element);
      default: throw new IllegalArgumentException(element.getTagName());
    }
  }
}
