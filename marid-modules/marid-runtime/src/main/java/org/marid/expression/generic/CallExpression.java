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

import org.marid.beans.BeanTypeContext;
import org.marid.types.Invokable;
import org.marid.types.InvokableConstructor;
import org.marid.types.InvokableMethod;
import org.marid.types.Types;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
    return invokable(this, owner, context, argTypes)
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
  static Optional<? extends Invokable<?>> invokable(@NotNull CallExpression e,
                                                    @Nullable Type owner,
                                                    @NotNull BeanTypeContext context,
                                                    @NotNull Type... argTypes) {
    if ("new".equals(e.getMethod())) {
      return e.getTarget().getTargetClass(owner, context)
          .flatMap(c -> Stream.of(c.getConstructors()))
          .map(InvokableConstructor::new)
          .filter(c -> matches(c, argTypes))
          .findFirst();
    } else {
      return e.getTarget().getTargetClass(owner, context)
          .flatMap(c -> Stream.of(c.getMethods()))
          .filter(m -> m.getName().equals(e.getMethod()))
          .map(InvokableMethod::new)
          .filter(m -> matches(m, argTypes))
          .findFirst();
    }
  }

  static boolean matches(Invokable<?> executable, Type... types) {
    if (executable.getParameterCount() == types.length) {
      for (int i = 0; i < types.length; i++) {
        if (!Types.isAssignable(types[i], executable.getParameterTypes()[i])) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }
}
