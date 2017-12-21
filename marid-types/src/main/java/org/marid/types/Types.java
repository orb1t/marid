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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.marid.collections.MaridSets;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;
import static org.marid.types.MaridWildcardType.ALL;

public interface Types {

  @Nullable
  static Type getArrayComponentType(@NotNull Type type) {
    return getArrayComponentType(type, emptySet());
  }

  private static Type getArrayComponentType(@NotNull Type type, @NotNull Set<TypeVariable<?>> passed) {
    if (type instanceof Class<?>) {
      return ((Class<?>) type).getComponentType();
    } else if (type instanceof GenericArrayType) {
      return ((GenericArrayType) type).getGenericComponentType();
    } else if (type instanceof TypeVariable<?>) {
      final TypeVariable<?> v = (TypeVariable<?>) type;
      if (passed.contains(v)) {
        return null;
      } else {
        final Set<TypeVariable<?>> newPassed = Sets.add(passed, v);
        return of(v.getBounds())
            .map(e -> getArrayComponentType(e, newPassed))
            .filter(Objects::nonNull)
            .reduce(Types::common)
            .orElse(null);
      }
    } else if (type instanceof WildcardType) {
      return of(((WildcardType) type).getUpperBounds())
          .map(e -> getArrayComponentType(e, passed))
          .filter(Objects::nonNull)
          .reduce(Types::common)
          .orElse(null);
    } else {
      return null;
    }
  }

  static boolean isGround(@NotNull Type type) {
    if (type instanceof Class<?>) {
      return true;
    } else if (type instanceof TypeVariable<?>) {
      return false;
    } else if (type instanceof GenericArrayType) {
      return isGround(((GenericArrayType) type).getGenericComponentType());
    } else if (type instanceof WildcardType) {
      final WildcardType wt = (WildcardType) type;
      return concat(of(wt.getUpperBounds()), of(wt.getLowerBounds())).allMatch(Types::isGround);
    } else if (type instanceof ParameterizedType) {
      return of(((ParameterizedType) type).getActualTypeArguments()).allMatch(Types::isGround);
    } else {
      throw new IllegalArgumentException("Unknown type: " + type);
    }
  }

  @NotNull
  static Set<TypeVariable<?>> vars(@NotNull Type type) {
    final LinkedHashSet<TypeVariable<?>> vars = new LinkedHashSet<>();
    vars(type, vars);
    return vars;
  }

  private static void vars(@NotNull Type type, @NotNull LinkedHashSet<TypeVariable<?>> vars) {
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

  @NotNull
  static Stream<Class<?>> rawClasses(@NotNull Type type) {
    final LinkedList<Class<?>> set = new LinkedList<>();
    raw(boxed(type), new HashSet<>(), c -> {
      if (set.stream().noneMatch(c::isAssignableFrom)) {
        set.add(c);
      }
    });
    return set.stream();
  }

  private static void raw(Type type, HashSet<TypeVariable<?>> passed, Consumer<Class<?>> v) {
    if (type instanceof Class<?>) {
      v.accept((Class<?>) type);
    } else if (type instanceof ParameterizedType) {
      v.accept((Class<?>) ((ParameterizedType) type).getRawType());
    } else if (type instanceof GenericArrayType) {
      v.accept(Object.class);
    } else if (type instanceof TypeVariable<?>) {
      if (passed.add((TypeVariable<?>) type)) {
        for (final Type bound : ((TypeVariable<?>) type).getBounds()) {
          raw(bound, passed, v);
        }
      }
    } else if (type instanceof WildcardType) {
      for (final Type bound : ((WildcardType) type).getUpperBounds()) {
        raw(bound, passed, v);
      }
    } else {
      throw new IllegalArgumentException(type.getTypeName());
    }
  }

  @NotNull
  static Type boxed(@NotNull Type type) {
    return type instanceof Class<?> ? Classes.wrapper((Class<?>) type) : type;
  }

  @NotNull
  static Type ground(@NotNull Type type, @NotNull Map<TypeVariable<?>, Type> map) {
    return ground(type, map, emptySet());
  }

  private static Type ground(Type type, Map<TypeVariable<?>, Type> map, Set<TypeVariable<?>> passed) {
    if (type instanceof Class<?> || isGround(type)) {
      return type;
    } else if (type instanceof GenericArrayType) {
      final GenericArrayType t = (GenericArrayType) type;
      final Type et = ground(t.getGenericComponentType(), map, passed);
      return et instanceof Class<?> ? Array.newInstance((Class<?>) et, 0).getClass() : new MaridArrayType(et);
    } else if (type instanceof ParameterizedType) {
      final ParameterizedType t = (ParameterizedType) type;
      final Type[] types = of(t.getActualTypeArguments()).map(e -> ground(e, map, passed)).toArray(Type[]::new);
      return new MaridParameterizedType(t.getOwnerType(), t.getRawType(), types);
    } else if (type instanceof WildcardType) {
      final WildcardType t = (WildcardType) type;
      final Type[] upper = of(t.getUpperBounds()).map(e -> ground(e, map, passed)).toArray(Type[]::new);
      final Type[] lower = of(t.getLowerBounds()).map(e -> ground(e, map, passed)).toArray(Type[]::new);
      return new MaridWildcardType(upper, lower);
    } else if (type instanceof TypeVariable<?>) {
      final TypeVariable<?> v = (TypeVariable<?>) type;
      final Set<TypeVariable<?>> p = MaridSets.add(passed, v);
      final Type t = map.get(v);
      if (t == null || t.equals(v) || p == passed) { // not found or circular reference
        final Type[] bounds = of(v.getBounds())
            .filter(e -> !(e instanceof TypeVariable<?>) || !p.contains(e))
            .map(e -> ground(e, map, p))
            .toArray(Type[]::new);
        switch (bounds.length) {
          case 0: return Object.class;
          case 1: return bounds[0];
          default: return new MaridWildcardType(bounds, new Type[0]);
        }
      } else {
        return ground(t, map, p);
      }
    } else {
      return type;
    }
  }

  @NotNull
  static  Type resolve(@NotNull Type type, @NotNull Map<TypeVariable<?>, Type> map) {
    return resolve(type, map, emptySet());
  }

  private static Type resolve(Type type, Map<TypeVariable<?>, Type> map, Set<TypeVariable<?>> passed) {
    if (type instanceof Class<?> || vars(type).stream().noneMatch(map::containsKey)) {
      return type;
    } else if (type instanceof ParameterizedType) {
      final ParameterizedType t = (ParameterizedType) type;
      final Type[] args = of(t.getActualTypeArguments()).map(e -> resolve(e, map, passed)).toArray(Type[]::new);
      return new MaridParameterizedType(t.getOwnerType(), t.getRawType(), args);
    } else if (type instanceof WildcardType) {
      final WildcardType t = (WildcardType) type;
      final Type[] upper = of(t.getUpperBounds()).map(e -> resolve(e, map, passed)).toArray(Type[]::new);
      final Type[] lower = of(t.getLowerBounds()).map(e -> resolve(e, map, passed)).toArray(Type[]::new);
      return new MaridWildcardType(upper, lower);
    } else if (type instanceof GenericArrayType) {
      final GenericArrayType t = (GenericArrayType) type;
      final Type et = resolve(t.getGenericComponentType(), map, passed);
      return et instanceof Class<?> ? Array.newInstance((Class<?>) et, 0).getClass() : new MaridArrayType(et);
    } else if (type instanceof TypeVariable<?>) {
      final TypeVariable<?> v = (TypeVariable<?>) type;
      for (Type t = map.get(v); t instanceof TypeVariable<?>; t = map.get(t)) {
        if (t.equals(v)) { // circular reference detected
          return v;
        }
      }
      final Type t = map.get(v);
      if (t == null) {
        return v;
      } else {
        final Set<TypeVariable<?>> p = Sets.add(passed, v);
        return p == passed ? v : resolve(t, map, p);
      }
    } else {
      throw new IllegalStateException("Unsupported type: " + type);
    }
  }

  static boolean isAssignable(@NotNull Type from, @NotNull Type to) {
    return isAssignable(boxed(from), boxed(to), new HashSet<>());
  }

  private static boolean isAssignable(Type from, Type to, HashSet<TypeVariable<?>> passed) {
    if (to.equals(from) || Object.class.equals(to) || void.class.equals(from)) {
      return true;
    }
    if (from instanceof WildcardType) {
      final WildcardType w = (WildcardType) from;
      return of(w.getUpperBounds()).anyMatch(t -> isAssignable(to, t, passed));
    }
    {
      final Type tt = getArrayComponentType(to);
      if (tt != null) {
        final Type ft = getArrayComponentType(from);
        return ft != null && isAssignable(ft, tt, passed);
      }
    }
    if (to instanceof Class<?>) {
      final Class<?> toClass = (Class<?>) to;
      if (from instanceof Class<?>) {
        return Classes.wrapper(toClass).isAssignableFrom(Classes.wrapper((Class<?>) from));
      } else if (from instanceof ParameterizedType) {
        final ParameterizedType t = (ParameterizedType) from;
        return isAssignable(t.getRawType(), toClass, passed);
      } else {
        return false;
      }
    }
    if (to instanceof TypeVariable<?>) {
      final TypeVariable<?> v = (TypeVariable<?>) to;
      return passed.add(v) && Arrays.stream(v.getBounds()).allMatch(t -> isAssignable(from, t, passed));
    }
    if (to instanceof WildcardType) {
      return Arrays.stream(((WildcardType) to).getUpperBounds()).allMatch(t -> isAssignable(from, t, passed));
    }
    if (to instanceof ParameterizedType) {
      final ParameterizedType t = (ParameterizedType) to;
      if (from instanceof Class<?>) {
        return isAssignable(t.getRawType(), from, passed) && of(t.getActualTypeArguments()).allMatch(ALL::equals);
      } else if (from instanceof ParameterizedType) {
        final ParameterizedType f = (ParameterizedType) from;
        if (isAssignable(t.getRawType(), f.getRawType(), passed)) {
          final Map<TypeVariable<?>, Type> mapFrom = resolveVars(from);
          final Map<TypeVariable<?>, Type> mapTo = resolveVars(to);
          for (final TypeVariable<?> v : ((Class<?>) t.getRawType()).getTypeParameters()) {
            final Type resolvedFrom = resolve(v, mapFrom);
            final Type resolvedTo = resolve(v, mapTo);
            if (!isAssignable(resolvedFrom, resolvedTo, passed)) {
              return false;
            }
          }
          return true;
        } else {
          return false;
        }
      } else {
        return false;
      }
    } else {
      throw new IllegalArgumentException("Unknown type: " + to);
    }
  }

  static Map<TypeVariable<?>, Type> resolveVars(@NotNull Type type) {
    final LinkedHashMap<TypeVariable<?>, Type> map = new LinkedHashMap<>();
    resolveVars(type, map);
    return map;
  }

  private static void resolveVars(Type type, Map<TypeVariable<?>, Type> map) {
    final Class<?> raw;
    if (type instanceof ParameterizedType) {
      final ParameterizedType pt = (ParameterizedType) type;
      raw = (Class<?>) pt.getRawType();
      final TypeVariable<?>[] vars = raw.getTypeParameters();
      final Type[] args = pt.getActualTypeArguments();
      if (args.length == vars.length) {
        for (int i = 0; i < vars.length; i++) {
          if (!vars[i].equals(args[i])) {
            if (args[i] instanceof TypeVariable<?>) {
              final TypeVariable<?> arg = (TypeVariable<?>) args[i];
              map.put(vars[i], map.getOrDefault(arg, args[i]));
            } else {
              map.put(vars[i], args[i]);
            }
          }
        }
      } else {
        throw new IllegalArgumentException("Illegal type: " + type);
      }
    } else if (type instanceof Class<?>) {
      raw = (Class<?>) type;
    } else {
      return;
    }
    for (final Type gt : raw.getGenericInterfaces()) {
      resolveVars(gt, map);
    }
    final Type superclass = raw.getGenericSuperclass();
    if (superclass != null) {
      resolveVars(raw.getGenericSuperclass(), map);
    }
  }

  @NotNull
  static Type getType(@NotNull Class<?> type) {
    final TypeVariable<?>[] vars = type.getTypeParameters();
    if (vars.length == 0) {
      return type;
    } else {
      return new MaridParameterizedType(null, type, vars);
    }
  }

  @NotNull
  static Type evaluate(@NotNull Consumer<BiConsumer<Type, Type>> callback, @NotNull Type type) {
    if (Types.isGround(type)) {
      return type;
    } else {
      final TypeEvaluator evaluator = new TypeEvaluator();
      callback.accept(evaluator);
      return evaluator.eval(type);
    }
  }

  @NotNull
  static Type common(@NotNull Type type1, @NotNull Type type2) {
    final Type t1 = boxed(type1), t2 = boxed(type2);
    if (t1.equals(t2)) {
      return t1;
    } else {
      final Type at1 = getArrayComponentType(t1), at2 = getArrayComponentType(t2);
      if (at1 != null && at2 != null) {
        final Type c = common(at1, at2);
        return c instanceof Class<?> ? Array.newInstance((Class<?>) c, 0).getClass() : new MaridArrayType(c);
      } else {
        final Set<Type> ts = concat(types(t1), types(t2))
            .filter(t -> Object.class != t && isAssignable(t1, t) && isAssignable(t2, t))
            .collect(toCollection(LinkedHashSet::new));
        ts.removeIf(t -> ts.stream().anyMatch(e -> e != t && isAssignable(e, t)));
        switch (ts.size()) {
          case 0: return Object.class;
          case 1: return ts.iterator().next();
          default: return new MaridWildcardType(ts.toArray(new Type[ts.size()]), new Type[0]);
        }
      }
    }
  }

  @NotNull
  static Stream<? extends Type> types(@NotNull Type type) {
    final Map<TypeVariable<?>, Type> map = resolveVars(type = boxed(type));
    return rawClasses(type).flatMap(Classes::classes)
        .distinct()
        .map(c -> {
          final TypeVariable<?>[] vars = c.getTypeParameters();
          return vars.length == 0 ? c : new MaridParameterizedType(null, c, vars);
        })
        .map(t -> resolve(t, map));
  }
}
