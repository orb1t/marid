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
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static org.apache.commons.lang3.reflect.TypeUtils.WILDCARD_ALL;


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
      if ("new".equals(getMethod())) { // constructor
        return TypeUtil.classType(targetType)
            .flatMap(tc -> Stream.of(TypeUtil.getRaw(tc).getConstructors())
                .filter(e -> TypeUtil.matches(this, e, owner, context))
                .findFirst()
                .map(m -> {
                  final Class<?> decl = m.getDeclaringClass();
                  final Type[] formals = m.getGenericParameterTypes();
                  final Type[] actuals = getArgs().stream().map(a -> a.getType(owner, context)).toArray(Type[]::new);
                  final Type type = context.resolve(formals, actuals, this, context.getType(decl));
                  return context.resolve(owner, type);
                }))
            .orElse(WILDCARD_ALL);
      } else { // static method
        return TypeUtil.classType(targetType)
            .flatMap(t -> Stream.of(TypeUtil.getRaw(t).getMethods())
                .filter(m -> m.getName().equals(getMethod()) && Modifier.isStatic(m.getModifiers()))
                .filter(e -> TypeUtil.matches(this, e, owner, context))
                .findFirst()
                .map(m -> {
                  final Type[] formals = m.getGenericParameterTypes();
                  final Type[] actuals = getArgs().stream().map(a -> a.getType(owner, context)).toArray(Type[]::new);
                  final Type type = context.resolve(formals, actuals, this, m.getGenericReturnType());
                  return context.resolve(owner, type);
                }))
            .orElse(WILDCARD_ALL);
      }
    } else { // virtual method
      return Stream.of(TypeUtil.getRaw(targetType).getMethods())
          .filter(m -> m.getName().equals(getMethod()) && !Modifier.isStatic(m.getModifiers()))
          .filter(e -> TypeUtil.matches(this, e, owner, context))
          .findFirst()
          .map(m -> {
            final Type[] formals = m.getGenericParameterTypes();
            final Type[] actuals = getArgs().stream().map(a -> a.getType(owner, context)).toArray(Type[]::new);
            final Type type = context.resolve(formals, actuals, this, m.getGenericReturnType());
            return context.resolve(owner, type);
          })
          .map(type -> context.resolve(targetType, type))
          .orElse(WILDCARD_ALL);
    }
  }

  @Override
  default void resolve(@Nonnull Type type, @Nonnull TypeContext context, @Nonnull BiConsumer<Type, Type> evaluator) {
    final Type[] ats = getArgs().stream().map(a -> a.getType(type, context)).toArray(Type[]::new);
    final Class<?>[] rts = Stream.of(ats).map(TypeUtil::getRaw).toArray(Class<?>[]::new);
    MaridRuntimeUtils.accessibleMethods(TypeUtil.getRaw(type))
        .filter(m -> m.getName().equals(getMethod()))
        .filter(m -> MaridRuntimeUtils.compatible(m, rts))
        .forEach(m -> {
          final Type[] ts = m.getGenericParameterTypes();
          for (int i = 0; i < ts.length; i++) {
            evaluator.accept(context.resolve(type, ts[i]), ats[i]);
          }
        });
  }
}
