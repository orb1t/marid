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

package org.marid.expression.generic;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.marid.beans.BeanTypeContext;
import org.marid.types.Types;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public interface ApplyExpression extends Expression {

  @NotNull
  Expression getTarget();

  @NotNull
  String getMethod();

  @NotNull
  String getType();

  @NotNull
  List<? extends MappedExpression> getArgs();

  @NotNull
  @Override
  default Type getType(@Nullable Type owner, @NotNull BeanTypeContext context) {
    final AtomicReference<Class<?>> itf = new AtomicReference<>();
    final Method[] sams = context.getClass(getType()).stream()
        .filter(Class::isInterface)
        .peek(itf::set)
        .flatMap(c -> Stream.of(c.getMethods()))
        .filter(m -> !m.isDefault() && m.getDeclaringClass().isInterface())
        .toArray(Method[]::new);
    if (sams.length == 0 || sams.length > 1) {
      context.throwError(new IllegalStateException("SAM method error"));
      return Object.class;
    } else {
      final Method sam = sams[0];
      final Parameter[] samParameters = sam.getParameters();
      final Type targetType = getTarget().getType(owner, context);
      final Type[] argTypes = getArgs().stream()
          .map(e -> e.getMappedIndex() >= 0
              ? samParameters[e.getMappedIndex()].getType()
              : e.getValue().getType(targetType, context))
          .toArray(Type[]::new);
      return CallExpression.invokable(getTarget(), getMethod(), owner, context, argTypes)
          .map(m -> {
            final Type type = Types.getType(itf.get());
            final Type result = Types.evaluate(e -> {
              for (int i = 0; i < getArgs().size(); i++) {
                final MappedExpression expression = getArgs().get(i);
                if (expression.getMappedIndex() >= 0) {
                  final Type formal = samParameters[expression.getMappedIndex()].getParameterizedType();
                  final Type actual = context.resolve(targetType, m.getParameterTypes()[i]);
                  e.accept(formal, actual);
                }
              }
            }, type);
            return context.resolve(owner, result);
          })
          .orElseGet(() -> {
            context.throwError(new IllegalStateException("No such invokable"));
            return Object.class;
          });
    }
  }
}
