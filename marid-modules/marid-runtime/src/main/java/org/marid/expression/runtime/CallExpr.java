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

import org.marid.expression.generic.CallExpression;
import org.marid.expression.xml.XmlExpression;
import org.marid.function.ToImmutableList;
import org.marid.runtime.context.BeanContext;
import org.marid.types.Invokable;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static java.util.stream.Collectors.joining;
import static org.marid.types.Classes.value;

public final class CallExpr extends Expr implements CallExpression {

  @Nonnull
  private final Expr target;

  @Nonnull
  private final String method;

  @Nonnull
  private final List<Expr> args;

  public CallExpr(@Nonnull Expr target, @Nonnull String method, @Nonnull Expr... args) {
    this.target = target;
    this.method = method;
    this.args = List.of(args);
  }

  CallExpr(@Nonnull Element element) {
    super(element);
    target = XmlExpression.target(element, Expr::of, ClassExpr::new, RefExpr::new);
    method = XmlExpression.method(element);
    args = XmlExpression.args(element, Expr::of, StringExpr::new, new ToImmutableList<>());
  }

  @Override
  protected Object execute(@Nullable Object self, @Nullable Type owner, @Nonnull BeanContext context) {
    final Type[] argTypes = getArgs().stream().map(a -> a.getType(owner, context)).toArray(Type[]::new);
    final Optional<? extends Invokable<?>> optional = CallExpression.invokable(this, owner, context, argTypes);
    if (optional.isPresent()) {
      final Invokable<?> invokable = optional.get();
      final Class<?>[] argClasses = invokable.getParameterClasses();
      final Object[] args = new Object[argClasses.length];
      for (int i = 0; i < args.length; i++) {
        args[i] = value(argClasses[i], this.args.get(i).evaluate(self, owner, context));
      }
      try {
        if (invokable.isStatic()) {
          return invokable.execute(null, args);
        } else {
          return invokable.execute(getTarget().evaluate(self, owner, context), args);
        }
      } catch (ReflectiveOperationException x) {
        context.throwError(new IllegalStateException(x));
        return null;
      }
    } else {
      context.throwError(new NoSuchElementException(getMethod()));
      return null;
    }
  }

  @Override
  @Nonnull
  public Expr getTarget() {
    return target;
  }

  @Override
  @Nonnull
  public String getMethod() {
    return method;
  }

  @Override
  @Nonnull
  public List<Expr> getArgs() {
    return args;
  }

  @Override
  public String toString() {
    return args.stream().map(Object::toString).collect(joining(",", target + "." + method + "(", ")"));
  }
}
