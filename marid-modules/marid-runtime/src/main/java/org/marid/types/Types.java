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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.*;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Optional.empty;
import static java.util.Optional.of;

public interface Types {

  static boolean isArrayType(@Nonnull Type type) {
    return type instanceof GenericArrayType || type instanceof Class<?> && ((Class<?>) type).isArray();
  }

  @Nullable
  static Type getArrayComponentType(@Nonnull Type type) {
    if (type instanceof GenericArrayType) {
      return ((GenericArrayType) type).getGenericComponentType();
    } else if (type instanceof Class<?>) {
      return ((Class<?>) type).getComponentType();
    } else {
      return null;
    }
  }

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

  static boolean isGround(@Nonnull Type type) {
    if (type instanceof Class<?>) {
      return true;
    } else if (type instanceof TypeVariable<?>) {
      return false;
    } else if (type instanceof GenericArrayType) {
      return isGround(((GenericArrayType) type).getGenericComponentType());
    } else if (type instanceof WildcardType) {
      final WildcardType wt = (WildcardType) type;
      final Predicate<Type[]> ground = ts -> Stream.of(ts).allMatch(Types::isGround);
      return ground.test(wt.getUpperBounds()) && ground.test(wt.getLowerBounds());
    } else if (type instanceof ParameterizedType) {
      return Stream.of(((ParameterizedType) type).getActualTypeArguments()).allMatch(Types::isGround);
    } else {
      throw new IllegalArgumentException("Unknown type: " + type);
    }
  }

  @Nonnull
  static Set<TypeVariable<?>> vars(@Nonnull Type type) {
    final LinkedHashSet<TypeVariable<?>> vars = new LinkedHashSet<>();
    vars(type, vars);
    return vars;
  }

  private static void vars(@Nonnull Type type, @Nonnull LinkedHashSet<TypeVariable<?>> vars) {
    if (type instanceof TypeVariable<?>) {
      final TypeVariable<?> v = (TypeVariable<?>) type;
      if (!vars.contains(v)) {
        vars.add(v);
        for (final Type bound : v.getBounds()) {
          vars(bound, vars);
        }
      }
    } else if (type instanceof GenericArrayType) {
      final GenericArrayType a = (GenericArrayType) type;
      vars(a.getGenericComponentType(), vars);
    } else if (type instanceof WildcardType) {
      final WildcardType w = (WildcardType) type;
      for (final Type upper : w.getUpperBounds()) {
        vars(upper, vars);
      }
      for (final Type lower : w.getLowerBounds()) {
        vars(lower, vars);
      }
    } else if (type instanceof ParameterizedType) {
      final ParameterizedType p = (ParameterizedType) type;
      for (final Type arg : p.getActualTypeArguments()) {
        vars(arg, vars);
      }
    }
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
      return Classes.wrapper((Class<?>) type);
    } else {
      return type;
    }
  }
}
