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

package org.marid.types;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.marid.runtime.context.MaridRuntimeUtils;
import org.marid.types.expression.TypedCallExpression;
import org.marid.types.expression.TypedExpression;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Optional.empty;
import static java.util.Optional.of;

public interface TypeUtil {

  @Nonnull
  static Optional<Type> classType(@Nonnull Type type) {
    if (type instanceof ParameterizedType) {
      final ParameterizedType pt = (ParameterizedType) type;
      final Type[] args = pt.getActualTypeArguments();
      return args.length == 1 && pt.getRawType() == Class.class ? of(args[0]) : empty();
    } else {
      return empty();
    }
  }

  @Nonnull
  static Type type(@Nonnull Type returnType,
                   @Nonnull Type[] argTypes,
                   @Nonnull List<? extends TypedExpression> args,
                   @Nullable Type owner,
                   @Nonnull TypeContext context) {
    if (returnType instanceof Class<?>) {
      return returnType;
    } else {
      return context.evaluate(e -> {
        for (int i = 0; i < argTypes.length; i++) {
          e.accept(argTypes[i], args.get(i).type(owner, context));
        }
      }, returnType);
    }
  }

  @Nonnull
  static Type type(@Nonnull Method method,
                   @Nonnull List<? extends TypedExpression> args,
                   @Nullable Type owner,
                   @Nonnull TypeContext context) {
    return type(method.getGenericReturnType(), method.getGenericParameterTypes(), args, owner, context);
  }

  @Nonnull
  static Type type(@Nonnull Constructor<?> constructor,
                   @Nonnull List<? extends TypedExpression> args,
                   @Nullable Type owner,
                   @Nonnull TypeContext context) {
    final Class<?> decl = constructor.getDeclaringClass();
    return type(context.getType(decl), constructor.getGenericParameterTypes(), args, owner, context);
  }

  @Nonnull
  static Optional<Class<?>> getClass(@Nonnull ClassLoader classLoader, @Nonnull String name) {
    try {
      return Optional.of(MaridRuntimeUtils.loadClass(name, classLoader, false));
    } catch (ClassNotFoundException x) {
      return Optional.empty();
    }
  }

  static boolean matches(@Nonnull TypedCallExpression expr,
                         @Nonnull Executable e,
                         @Nullable Type owner,
                         @Nonnull TypeContext context) {
    if (e.getParameterCount() == expr.getArgs().size()) {
      final Type[] pt = e.getGenericParameterTypes();
      for (int i = 0; i < pt.length; i++) {
        final Type at = expr.getArgs().get(i).type(owner, context);
        if (!context.isAssignable(at, pt[i])) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }

  @Nonnull
  static Type resolve(@Nonnull TypedExpression expression, @Nonnull Type type, @Nonnull TypeContext context) {
    if (type instanceof Class<?>) {
      return type;
    } else {
      return context.evaluate(e -> expression.getInitializers().forEach(i -> i.resolve(type, context, e)), type);
    }
  }

  static boolean isGround(@Nonnull Type type) {
    if (type instanceof TypeVariable<?>) {
      return false;
    } else if (type instanceof GenericArrayType) {
      return isGround(((GenericArrayType) type).getGenericComponentType());
    } else if (type instanceof WildcardType) {
      final WildcardType wt = (WildcardType) type;
      final Predicate<Type[]> ground = ts -> Stream.of(ts).allMatch(TypeUtil::isGround);
      return ground.test(wt.getUpperBounds()) && ground.test(wt.getLowerBounds());
    } else if (type instanceof ParameterizedType) {
      return Stream.of(((ParameterizedType) type).getActualTypeArguments()).allMatch(TypeUtil::isGround);
    } else {
      return true;
    }
  }

  @Nonnull
  static Stream<TypeVariable<?>> vars(@Nonnull Type type) {
    if (type instanceof TypeVariable<?>) {
      return Stream.of((TypeVariable<?>) type);
    } else if (type instanceof GenericArrayType) {
      return vars(((GenericArrayType) type).getGenericComponentType());
    } else if (type instanceof WildcardType) {
      final WildcardType wt = (WildcardType) type;
      final Function<Type[], Stream<TypeVariable<?>>> vars = ts -> Stream.of(ts).flatMap(TypeUtil::vars);
      return Stream.concat(vars.apply(wt.getUpperBounds()), vars.apply(wt.getLowerBounds()));
    } else if (type instanceof ParameterizedType) {
      return Stream.of(((ParameterizedType) type).getActualTypeArguments()).flatMap(TypeUtil::vars);
    } else {
      return Stream.empty();
    }
  }

  @Nonnull
  static Type ground(@Nonnull Type type, @Nonnull TypeContext context) {
    return context.evaluate(e -> vars(type).forEach(t -> e.accept(t, varBound(t))), type);
  }

  @Nonnull
  static Type varBound(@Nonnull TypeVariable<?> variable) {
    return Stream.of(variable.getBounds()).findFirst().orElse(Object.class);
  }

  @Nonnull
  static Class<?> getRaw(@Nonnull Type type) {
    if (type instanceof Class<?>) {
      return (Class<?>) type;
    } else if (type instanceof ParameterizedType) {
      return (Class<?>) ((ParameterizedType) type).getRawType();
    } else if (type instanceof WildcardType) {
      return getRaw(((WildcardType) type).getUpperBounds()[0]);
    } else if (type instanceof GenericArrayType) {
      return Array.newInstance(getRaw(((GenericArrayType) type).getGenericComponentType()), 0).getClass();
    } else if (type instanceof TypeVariable<?>) {
      return getRaw(((TypeVariable<?>) type).getBounds()[0]);
    } else {
      throw new IllegalArgumentException(type.getTypeName());
    }
  }

  @Nonnull
  static Type boxed(@Nonnull Type type) {
    if (type instanceof Class<?> && ((Class<?>) type).isPrimitive()) {
      return MaridRuntimeUtils.wrapper((Class<?>) type);
    } else {
      return type;
    }
  }

  @Nonnull
  static ParameterizedType parameterize(@Nonnull Class<?> type, @Nonnull Type... parameters) {
    return TypeUtils.parameterize(type, Arrays.copyOf(parameters, parameters.length, Type[].class));
  }
}
