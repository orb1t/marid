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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.marid.expression.generic.ApplyExpression;
import org.marid.expression.xml.XmlExpression;
import org.marid.function.ToImmutableList;
import org.marid.runtime.context.BeanContext;
import org.w3c.dom.Element;

import java.lang.reflect.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.marid.expression.generic.CallExpression.invokable;

public class ApplyExpr extends Expr implements ApplyExpression {

  @NotNull
  private final Expr target;

  @NotNull
  private final String method;

  @NotNull
  private final String type;

  @NotNull
  private final List<MappedExpr> args;

  public ApplyExpr(@NotNull Expr target, @NotNull String method, @NotNull String type, @NotNull MappedExpr... args) {
    this.target = target;
    this.method = method;
    this.type = type;
    this.args = List.of(args);
  }

  ApplyExpr(@NotNull Element element) {
    this.target = XmlExpression.target(element, Expr::of, ClassExpr::new, RefExpr::new);
    this.method = XmlExpression.method(element);
    this.type = XmlExpression.type(element);
    this.args = XmlExpression.mappedArgs(element, Expr::of, MappedExpr::new, new ToImmutableList<>());
  }

  @NotNull
  @Override
  public Expr getTarget() {
    return target;
  }

  @NotNull
  @Override
  public String getMethod() {
    return method;
  }

  @NotNull
  @Override
  public String getType() {
    return type;
  }

  @NotNull
  @Override
  public List<MappedExpr> getArgs() {
    return args;
  }

  @Override
  protected Object execute(@Nullable Object self, @Nullable Type selfType, @NotNull BeanContext context) {
    final AtomicReference<Class<?>> itf = new AtomicReference<>();
    final Method[] sams = context.getClass(getType()).stream()
        .filter(Class::isInterface)
        .peek(itf::set)
        .flatMap(c -> Stream.of(c.getMethods()))
        .filter(m -> !m.isDefault() && m.getDeclaringClass().isInterface() && !Modifier.isStatic(m.getModifiers()))
        .toArray(Method[]::new);
    if (sams.length == 0 || sams.length > 1) {
      context.throwError(new IllegalStateException("SAM method error"));
      return Object.class;
    } else {
      final Method sam = sams[0];
      final Parameter[] samParameters = sam.getParameters();
      final Type targetType = getTarget().getType(selfType, context);
      final Type[] argTypes = getArgs().stream()
          .map(e -> e.getMappedIndex() >= 0
              ? samParameters[e.getMappedIndex()].getParameterizedType()
              : e.getValue().getType(targetType, context))
          .toArray(Type[]::new);
      return invokable(getTarget().getTargetClass(selfType, context), getMethod(), argTypes)
          .map(method -> {
            final Object[] params = getArgs().stream()
                .map(a -> a.getMappedIndex() >= 0 ? null : a.getValue().evaluate(self, selfType, context))
                .toArray();
            final int[] mapping = IntStream.range(0, getArgs().size())
                .filter(i -> getArgs().get(i).getMappedIndex() >= 0)
                .flatMap(i -> IntStream.of(i, getArgs().get(i).getMappedIndex()))
                .toArray();
            if (mapping.length == 0) {
              return Proxy.newProxyInstance(context.getClassLoader(), new Class<?>[]{itf.get()}, (p, m, args) -> {
                if (m.equals(sam)) {
                  return method.execute(p, params);
                } else {
                  return m.invoke(p, args);
                }
              });
            } else {
              return Proxy.newProxyInstance(context.getClassLoader(), new Class<?>[]{itf.get()}, (p, m, args) -> {
                if (m.equals(sam)) {
                  final Object[] ps = params.clone();
                  for (int i = 0; i < mapping.length; i += 2) {
                    ps[mapping[i]] = args[mapping[i + 1]];
                  }
                  return method.execute(p, ps);
                } else {
                  return m.invoke(p, args);
                }
              });
            }
          })
          .orElseThrow(() -> new IllegalStateException("Unable to find an invokable method"));
    }
  }
}
