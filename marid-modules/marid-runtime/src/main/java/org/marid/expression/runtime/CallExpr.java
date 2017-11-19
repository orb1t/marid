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
import org.marid.runtime.context.BeanContext;
import org.marid.types.Types;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.marid.io.Xmls.*;
import static org.marid.types.Classes.*;

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
    this.args = Arrays.asList(args);
  }

  CallExpr(@Nonnull Element element) {
    super(element);
    target = element("target", element).map(Expr::of).orElseThrow(() -> new NullPointerException("target"));
    method = attribute(element, "method").orElseThrow(() -> new NullPointerException("method"));
    args = elements("args", element).map(Expr::of).collect(toList());
  }

  @Override
  protected Object execute(@Nullable Object self, @Nullable Type owner, @Nonnull BeanContext context) {
    final Object target = getTarget().evaluate(self, owner, context);
    final Class<?> targetClass = getTarget().getTargetClass(owner, context);
    final Type[] argTypes = getArgs().stream().map(a -> a.getType(owner, context)).toArray(Type[]::new);
    if ("new".equals(getMethod())) {
      final Constructor<?> constructor = Stream.of(targetClass.getConstructors())
          .filter(c -> matches(c, argTypes))
          .findFirst()
          .orElseThrow(() -> new NoSuchElementException(getMethod() + " in " + context));
      try {
        final Class<?>[] argClasses = constructor.getParameterTypes();
        final Object[] args = new Object[argClasses.length];
        for (int i = 0; i < args.length; i++) {
          args[i] = value(argClasses[i], this.args.get(i).evaluate(self, owner, context));
        }
        return constructor.newInstance(args(constructor, args));
      } catch (ReflectiveOperationException x) {
        throw new IllegalStateException(x);
      }
    } else {
      final Method method = Stream.of(targetClass.getMethods())
          .filter(m -> m.getName().equals(getMethod()) && matches(m, argTypes))
          .findFirst()
          .orElseThrow(() -> new NoSuchElementException(getMethod() + " in " + context));
      try {
        final Class<?>[] argClasses = method.getParameterTypes();
        final Object[] args = new Object[argClasses.length];
        for (int i = 0; i < args.length; i++) {
          args[i] = value(argClasses[i], this.args.get(i).evaluate(self, owner, context));
        }
        return method.invoke(target, args);
      } catch (ReflectiveOperationException x) {
        throw new IllegalStateException(x);
      }
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

  private boolean matches(Executable executable, Type... types) {
    if (executable.getParameterCount() == types.length) {
      final Class<?>[] ts = executable.getParameterTypes();
      return range(0, ts.length).allMatch(i -> compatible(ts[i], Types.getRaw(types[i])));
    } else {
      return false;
    }
  }
}
