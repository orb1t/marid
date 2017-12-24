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
import org.marid.types.invokable.Invokable;
import org.marid.types.invokable.InvokableConstructor;
import org.marid.types.invokable.InvokableField;
import org.marid.types.invokable.InvokableMethod;

import java.lang.reflect.Type;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public interface CallExpression extends Expression {

  @NotNull
  Expression getTarget();

  @NotNull
  String getMethod();

  @NotNull
  List<? extends Expression> getArgs();

  @NotNull
  @Override
  default Type getType(@Nullable Type owner, @NotNull BeanTypeContext context) {
    final Type[] argTypes = getArgs().stream().map(a -> a.getType(owner, context)).toArray(Type[]::new);
    return invokable(getTarget().getTargetClass(owner, context), getMethod(), argTypes)
        .map(invokable -> {
          final Type r = context.resolve(invokable.getParameterTypes(), argTypes, this, invokable.getReturnType());
          if (invokable.isStatic()) {
            return r;
          } else {
            final Type type = getTarget().getType(owner, context);
            return context.resolve(type, r);
          }
        })
        .orElseGet(() -> {
          context.throwError(new NoSuchElementException(getMethod()));
          return Object.class;
        });
  }

  @Override
  default void resolve(@NotNull Type type, @NotNull BeanTypeContext context, @NotNull BiConsumer<Type, Type> evaluator) {
    if (getTarget() instanceof ThisExpression) {
      final Type[] ats = getArgs().stream().map(a -> a.getType(type, context)).toArray(Type[]::new);
      Types.rawClasses(type).flatMap(c -> Stream.of(c.getMethods()))
          .filter(m -> m.getName().equals(getMethod()))
          .map(InvokableMethod::new)
          .filter(i -> matches(i, ats))
          .findFirst()
          .ifPresent(invokable -> {
            final Type[] ts = invokable.getParameterTypes();
            for (int i = 0; i < ts.length; i++) {
              evaluator.accept(context.resolve(type, ts[i]), ats[i]);
            }
          });
    }
  }

  @NotNull
  static Optional<Invokable> invokable(@NotNull Stream<Class<?>> classes, @NotNull String method, @NotNull Type... argTypes) {
    return classes
        .flatMap(c -> {
          if ("new".equals(method)) {
            return Stream.of(c.getConstructors()).map(InvokableConstructor::new);
          } else {
            return Stream.concat(
                Stream.of(c.getMethods()).filter(m -> m.getName().equals(method)).map(InvokableMethod::new),
                Stream.of(c.getFields()).filter(f -> f.getName().equals(method)).flatMap(InvokableField::invokables)
            );
          }
        })
        .filter(i -> matches(i, argTypes))
        .findFirst();
  }

  static boolean matches(Invokable invokable, Type... types) {
    if (invokable.getParameterCount() == types.length) {
      for (int i = 0; i < types.length; i++) {
        if (!Types.isAssignable(invokable.getParameterTypes()[i], types[i])) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }
}
