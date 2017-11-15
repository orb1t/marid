/*-
 * #%L
 * marid-types
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

package org.marid.types.expression;

import org.marid.expression.generic.CallExpression;
import org.marid.expression.generic.ClassExpression;
import org.marid.runtime.context.MaridRuntimeUtils;
import org.marid.types.TypeContext;
import org.marid.types.TypeUtil;

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
import static org.apache.commons.lang3.reflect.TypeUtils.WILDCARD_ALL;
import static org.marid.types.TypeUtil.classType;
import static org.marid.types.TypeUtil.getRaw;

public interface TypedCallExpression extends CallExpression, TypedExpression {

  @Nonnull
  @Override
  TypedExpression getTarget();

  @Nonnull
  @Override
  List<? extends TypedExpression> getArgs();

  @Nonnull
  @Override
  default Type getType(@Nullable Type owner, @Nonnull TypeContext context) {
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
        return WILDCARD_ALL;
      } else { // static method
        for (final Method m : targetRaw.getMethods()) {
          if (m.getName().equals(getMethod()) && isStatic(m.getModifiers()) && matches(m, owner, context)) {
            final Type[] formals = m.getGenericParameterTypes();
            final Type[] actuals = getArgs().stream().map(a -> a.getType(owner, context)).toArray(Type[]::new);
            final Type type = context.resolve(formals, actuals, this, m.getGenericReturnType());
            return context.resolve(owner, type);
          }
        }
        return WILDCARD_ALL;
      }
    } else { // virtual method
      for (final Method m : TypeUtil.getRaw(targetType).getMethods()) {
        if (m.getName().equals(getMethod()) && !isStatic(m.getModifiers()) && matches(m, owner, context)) {
          final Type[] formals = m.getGenericParameterTypes();
          final Type[] actuals = getArgs().stream().map(a -> a.getType(owner, context)).toArray(Type[]::new);
          final Type type = context.resolve(formals, actuals, this, m.getGenericReturnType());
          return context.resolve(owner, type);
        }
      }
      return WILDCARD_ALL;
    }
  }

  @Override
  default void resolve(@Nonnull Type type, @Nonnull TypeContext context, @Nonnull BiConsumer<Type, Type> evaluator) {
    if (getTarget() instanceof TypedThisExpression) {
      final Type[] ats = getArgs().stream().map(a -> a.getType(type, context)).toArray(Type[]::new);
      final Class<?>[] rts = Stream.of(ats).map(TypeUtil::getRaw).toArray(Class<?>[]::new);
      for (final Method method : TypeUtil.getRaw(type).getMethods()) {
        if (method.getName().equals(getMethod()) && MaridRuntimeUtils.compatible(method, rts)) {
          final Type[] ts = method.getGenericParameterTypes();
          for (int i = 0; i < ts.length; i++) {
            evaluator.accept(context.resolve(type, ts[i]), ats[i]);
          }
        }
      }
    }
  }

  private boolean matches(@Nonnull Executable e, @Nullable Type owner, @Nonnull TypeContext context) {
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
