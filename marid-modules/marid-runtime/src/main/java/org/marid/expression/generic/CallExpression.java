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
import org.marid.types.Types;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static java.lang.reflect.Modifier.isStatic;
import static java.util.stream.Stream.of;
import static org.marid.types.MaridWildcardType.ALL;
import static org.marid.types.Types.classType;
import static org.marid.types.Types.getRaw;

public interface CallExpression extends Expression {

  @Nonnull
  Expression getTarget();

  @Nonnull
  String getMethod();

  @Nonnull
  List<? extends Expression> getArgs();

  @Nonnull
  @Override
  default Type getType(@Nullable Type owner, @Nonnull BeanTypeContext context) {
    final Type targetType = getTarget().getType(owner, context);
    if (getTarget() instanceof ClassExpression) { // static call
      final Class<?> targetRaw = classType(targetType).map(t -> (Class) getRaw(t)).orElse(void.class);
      if ("new".equals(getMethod())) { // constructor
        for (final Constructor c : targetRaw.getConstructors()) {
          if (matches(c, owner, context)) {
            final Class<?> decl = c.getDeclaringClass();
            final Type[] formals = c.getGenericParameterTypes();
            final Type[] actuals = getArgs().stream().map(a -> a.getType(owner, context)).toArray(Type[]::new);
            final Type type = context.resolve(formals, actuals, this, context.getType(decl));
            return context.resolve(owner, type);
          }
        }
        return ALL;
      } else { // static method
        for (final Method m : targetRaw.getMethods()) {
          if (m.getName().equals(getMethod()) && isStatic(m.getModifiers()) && matches(m, owner, context)) {
            final Type[] formals = m.getGenericParameterTypes();
            final Type[] actuals = getArgs().stream().map(a -> a.getType(owner, context)).toArray(Type[]::new);
            final Type type = context.resolve(formals, actuals, this, m.getGenericReturnType());
            return context.resolve(owner, type);
          }
        }
        return ALL;
      }
    } else { // virtual method
      for (final Method m : Types.getRaw(targetType).getMethods()) {
        if (m.getName().equals(getMethod()) && !isStatic(m.getModifiers()) && matches(m, owner, context)) {
          final Type[] formals = m.getGenericParameterTypes();
          final Type[] actuals = getArgs().stream().map(a -> a.getType(owner, context)).toArray(Type[]::new);
          final Type type = context.resolve(formals, actuals, this, m.getGenericReturnType());
          return context.resolve(owner, type);
        }
      }
      return ALL;
    }
  }

  @Override
  default void resolve(@Nonnull Type type, @Nonnull BeanTypeContext context, @Nonnull BiConsumer<Type, Type> evaluator) {
    if (getTarget() instanceof ThisExpression) {
      final Type[] ats = getArgs().stream().map(a -> a.getType(type, context)).toArray(Type[]::new);
      for (final Method method : Types.getRaw(type).getMethods()) {
        if (method.getName().equals(getMethod()) && matches(method, null, context)) {
          final Type[] ts = method.getGenericParameterTypes();
          for (int i = 0; i < ts.length; i++) {
            evaluator.accept(context.resolve(type, ts[i]), ats[i]);
          }
        }
      }
    }
  }

  default boolean isArgAssignableFrom(@Nonnull Type type, int arg, @Nullable Type owner, @Nonnull BeanTypeContext context) {
    final Type targetType = getTarget().getType(owner, context);
    final Stream<? extends Executable> executables;
    if (getTarget() instanceof ClassExpression) {
      final Class<?> targetRaw = classType(targetType).map(t -> (Class) getRaw(t)).orElse(void.class);
      executables = !"new".equals(getMethod())
          ? of(targetRaw.getMethods()).filter(m -> m.getName().equals(getMethod()) && isStatic(m.getModifiers()))
          : of(targetRaw.getConstructors());
    } else {
      executables = of(Types.getRaw(targetType).getMethods())
          .filter(m -> m.getName().equals(getMethod()))
          .filter(m -> !isStatic(m.getModifiers()));
    }
    return executables
        .filter(e -> matches(e, owner, context))
        .findFirst()
        .filter(e -> {
          final Type argType = e.getGenericParameterTypes()[arg];
          return context.isAssignable(type, argType);
        })
        .isPresent();
  }

  private boolean matches(@Nonnull Executable e, @Nullable Type owner, @Nonnull BeanTypeContext context) {
    if (e.getParameterCount() == getArgs().size()) {
      final Type[] pt = e.getGenericParameterTypes();
      for (int i = 0; i < pt.length; i++) {
        final Type at = getArgs().get(i).getType(owner, context);
        if (!context.isAssignable(at, pt[i])) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }
}
