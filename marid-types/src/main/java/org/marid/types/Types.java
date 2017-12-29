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
import org.marid.types.invokable.Invokables;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.Collections.emptySet;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;

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
      final GenericArrayType a = (GenericArrayType) type;
      rawClasses(a.getGenericComponentType()).forEach(c -> v.accept(Array.newInstance(c, 0).getClass()));
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
      return t == null || passed.contains(v) ? v : resolve(t, map, Sets.add(passed, v));
    } else {
      throw new IllegalStateException("Unsupported type: " + type);
    }
  }

  static boolean isAssignable(@NotNull Type to, @NotNull Type from) {
    return isAssignable(to, from, emptySet(), emptySet());
  }

  private static boolean isAssignable(Type to, Type from, Set<TypeVariable<?>> pt, Set<TypeVariable<?>> pf) {
    if (to.equals(from) || Object.class.equals(to) || void.class.equals(from)) {
      return true;
    }
    if (to instanceof WildcardType) {
      final WildcardType tw = (WildcardType) to;
      return of(tw.getUpperBounds()).allMatch(t -> isAssignable(t, from, pt, pf));
    }
    if (to instanceof ParameterizedType) {
      final ParameterizedType tp = (ParameterizedType) to;
      if (from instanceof Class<?>) {
        if (!isAssignable(tp.getRawType(), from, pt, pf)) {
          return false;
        }
        final Type[] args = tp.getActualTypeArguments();
        if (of(args).allMatch(MaridWildcardType::isAll)) {
          return true;
        }
        final Class<?> tRaw = (Class<?>) tp.getRawType();
        final TypeVariable<?>[] tVars = tRaw.getTypeParameters();
        if (tVars.length == args.length) {
          final Map<TypeVariable<?>, Type> map = resolveVars(from);
          for (int i = 0; i < tVars.length; i++) {
            final TypeVariable<?> var = tVars[i];
            final Type arg = args[i];
            final Type resolvedVar = resolve(var, map);
            if (!isAssignable(arg, resolvedVar)) {
              return false;
            }
          }
          return true;
        } else {
          throw new IllegalArgumentException("Illegal type: " + to.getTypeName());
        }
      } else if (from instanceof ParameterizedType) {
        final ParameterizedType fp = (ParameterizedType) from;
        if (!isAssignable(tp.getRawType(), fp.getRawType())) {
          return false;
        }
        final Map<TypeVariable<?>, Type> fVars = resolveVars(from);
        final Map<TypeVariable<?>, Type> tVars = resolveVars(to);
        for (final TypeVariable<?> v : ((Class<?>) tp.getRawType()).getTypeParameters()) {
          final Type t = resolve(v, tVars);
          final Type f = resolve(v, fVars);
          if (!isAssignable(t, f, pt, pf)) {
            return false;
          }
        }
        return true;
      } else if (getArrayComponentType(from) != null) {
        return false;
      }
    }
    {
      final Type ct = getArrayComponentType(to);
      if (ct != null) {
        final Type cf = getArrayComponentType(from);
        return cf != null && isAssignable(boxed(ct), boxed(cf), pt, pf);
      }
    }
    if (to instanceof Class<?>) {
      if (from instanceof Class<?>) {
        final Class<?> t = Classes.wrapper((Class<?>) to);
        final Class<?> f = Classes.wrapper((Class<?>) from);
        return t.isAssignableFrom(f);
      } else if (from instanceof ParameterizedType) {
        return isAssignable(((ParameterizedType) from).getRawType(), to, pt, pf);
      } else if (from instanceof GenericArrayType) {
        return false;
      }
    }
    if (to instanceof TypeVariable<?>) {
      final TypeVariable<?> tv = (TypeVariable<?>) to;
      if (pt.contains(tv)) {
        return true;
      } else {
        final Set<TypeVariable<?>> npt = Sets.add(pt, tv);
        return of(tv.getBounds()).allMatch(t -> isAssignable(t, from, npt, pf));
      }
    }
    if (from instanceof WildcardType) {
      final WildcardType fw = (WildcardType) from;
      return of(fw.getUpperBounds()).anyMatch(f -> isAssignable(to, f, pt, pf));
    }
    if (from instanceof TypeVariable<?>) {
      final TypeVariable<?> fv = (TypeVariable<?>) from;
      if (pf.contains(fv)) {
        return true;
      } else {
        final Set<TypeVariable<?>> npf = Sets.add(pf, fv);
        return of(fv.getBounds()).anyMatch(f -> isAssignable(to, f, pt, npf));
      }
    }
    return false;
  }

  static Map<TypeVariable<?>, Type> resolveVars(@NotNull Type type) {
    final LinkedHashMap<TypeVariable<?>, Type> map = new LinkedHashMap<>();
    resolveVars(type, map);
    return map;
  }

  private static void resolveVars(Type type, LinkedHashMap<TypeVariable<?>, Type> map) {
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
        throw new IllegalArgumentException("Illegal type: " + type.getTypeName());
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
        final ConcurrentLinkedQueue<Type> ts = concat(typesTree(t1), typesTree(t2))
            .filter(t -> isAssignable(t, t1) && isAssignable(t, t2))
            .distinct()
            .collect(Collectors.toCollection(ConcurrentLinkedQueue::new));
        ts.removeIf(t -> ts.stream().anyMatch(e -> e != t && isAssignable(t, e)));
        switch (ts.size()) {
          case 0: return Object.class;
          case 1: return ts.iterator().next();
          default: return new MaridWildcardType(ts.toArray(new Type[ts.size()]), new Type[0]);
        }
      }
    }
  }

  @NotNull
  static Stream<? extends Type> typesTree(@NotNull Type type) {
    final Map<TypeVariable<?>, Type> map = resolveVars(type = boxed(type));
    return rawClasses(type).flatMap(Classes::classes)
        .distinct()
        .map(c -> {
          final TypeVariable<?>[] vars = c.getTypeParameters();
          return vars.length == 0 ? c : new MaridParameterizedType(null, c, vars);
        })
        .map(t -> resolve(t, map));
  }

  @NotNull
  static Type apply(@NotNull Class<?> type, @NotNull Type target, @NotNull String method, @NotNull int[] indices, @NotNull Type... args) {
    final TypeVariable<?>[] vars = type.getTypeParameters();
    if (vars.length == 0) {
      return type;
    } else {
      return Classes.getSam(type)
          .flatMap(m -> Types.rawClasses(target)
              .flatMap(c -> Invokables.invokables(c, method))
              .filter(i -> i.matches(args))
              .findFirst()
              .map(i -> {
                final Type[] samArgs = m.getGenericParameterTypes();
                final Map<TypeVariable<?>, Type> typeVars = Types.resolveVars(target);
                return Types.evaluate(e -> {
                  for (int k = 0; k < indices.length; k++) {
                    final int index = indices[k];
                    if (index >= 0 && index < i.getParameterCount()) {
                      final Type actual = Types.resolve(i.getParameterTypes()[index], typeVars);
                      final Type formal = samArgs[k];
                      e.accept(formal, actual);
                    }
                  }
                  IntStream.range(0, i.getParameterCount()).forEach(k -> {
                    if (stream(indices).noneMatch(v -> v != k)) {
                      final Type formal = i.getParameterTypes()[k];
                      final Type actual = args[k];
                      e.accept(formal, actual);
                    }
                  });
                  e.accept(m.getGenericReturnType(), Types.resolve(i.getReturnType(), typeVars));
                }, new MaridParameterizedType(null, type, vars));
              })
          )
          .orElse(type);
    }
  }
}
