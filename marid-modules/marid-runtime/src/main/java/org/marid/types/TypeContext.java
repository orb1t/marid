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

package org.marid.types;

import org.marid.collections.MaridSets;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;
import static org.marid.types.Classes.compatible;
import static org.marid.types.MaridWildcardType.ALL;
import static org.marid.types.Types.*;

public class TypeContext {

  public <E extends Throwable> void throwError(@Nonnull E exception) throws E {
    throw exception;
  }

  public ClassLoader getClassLoader() {
    return Thread.currentThread().getContextClassLoader();
  }

  @Nonnull
  public Type resolve(@Nullable Type owner, @Nonnull Type type) {
    if (owner == null) {
      return type;
    } else {
      return resolve(type, resolveVars(owner));
    }
  }

  @Nonnull
  public Class<?> getClass(@Nonnull String name) {
    try {
      return Classes.loadClass(name, getClassLoader(), false);
    } catch (ClassNotFoundException x) {
      throwError(new IllegalStateException(x));
    } catch (RuntimeException x) {
      throwError(x);
    }
    return Object.class;
  }

  @Nonnull
  public Type ground(@Nonnull Type type, @Nonnull Map<TypeVariable<?>, Type> map) {
    return ground(type, map, emptySet());
  }

  private Type ground(Type type, Map<TypeVariable<?>, Type> map, Set<TypeVariable<?>> passed) {
    if (type instanceof Class<?> || isGround(type)) {
      return type;
    } else if (type instanceof GenericArrayType) {
      final GenericArrayType t = (GenericArrayType) type;
      final Type et = ground(t.getGenericComponentType(), map, passed);
      return et instanceof Class<?> ? Array.newInstance((Class<?>) et, 0).getClass() : new MaridArrayType(et);
    } else if (type instanceof ParameterizedType) {
      final ParameterizedType t = (ParameterizedType) type;
      final Type[] types = Stream.of(t.getActualTypeArguments()).map(e -> ground(e, map, passed)).toArray(Type[]::new);
      return new MaridParameterizedType(t.getOwnerType(), t.getRawType(), types);
    } else if (type instanceof WildcardType) {
      final WildcardType t = (WildcardType) type;
      final Type[] upper = of(t.getUpperBounds()).map(e -> ground(e, map, passed)).toArray(Type[]::new);
      final Type[] lower = of(t.getLowerBounds()).map(e -> ground(e, map, passed)).toArray(Type[]::new);
      return new MaridWildcardType(upper, lower);
    } else if (type instanceof TypeVariable<?>) {
      final TypeVariable<?> v = (TypeVariable<?>) type;
      final Set<TypeVariable<?>> p = MaridSets.add(passed, v, HashSet::new);
      final Type t = map.get(v);
      if (t == null || t.equals(v) || p.size() == passed.size()) { // not found or circular reference
        final Type[] bounds = Stream.of(v.getBounds())
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

  @Nonnull
  public Type resolve(@Nonnull Type type, @Nonnull Map<TypeVariable<?>, Type> map) {
    return resolve(type, map, emptySet());
  }

  private Type resolve(Type type, Map<TypeVariable<?>, Type> map, Set<TypeVariable<?>> passed) {
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
      for (Type t = map.getOrDefault(v, v); t instanceof TypeVariable<?>; t = map.getOrDefault(t, t)) {
        if (t.equals(v)) { // absent var or circular reference detected
          return v;
        }
      }
      final Set<TypeVariable<?>> p = MaridSets.add(passed, v, HashSet::new);
      return p.size() == passed.size() ? v : resolve(map.get(v), map, p);
    } else {
      throw new IllegalStateException("Unsupported type: " + type);
    }
  }

  public boolean isAssignable(@Nonnull Type from, @Nonnull Type to) {
    return isAssignable(from, to, new HashSet<>());
  }

  private boolean isAssignable(@Nonnull Type from, @Nonnull Type to, @Nonnull HashSet<TypeVariable<?>> passed) {
    if (to.equals(from) || Object.class.equals(to)) {
      return true;
    } if (from instanceof WildcardType) {
      final WildcardType w = (WildcardType) from;
      return of(w.getUpperBounds()).anyMatch(t -> isAssignable(to, t, passed));
    } else if (Types.isArrayType(to)) {
      if (Types.isArrayType(from)) {
        final Type fromCt = requireNonNull(getArrayComponentType(from));
        final Type toCt = requireNonNull(getArrayComponentType(to));
        return isAssignable(fromCt, toCt, passed);
      } else {
        return false;
      }
    } else if (to instanceof Class<?>) {
      final Class<?> toClass = (Class<?>) to;
      if (from instanceof Class<?>) {
        return compatible(toClass, (Class<?>) from);
      } else if (from instanceof ParameterizedType) {
        final ParameterizedType t = (ParameterizedType) from;
        final Class<?> raw = (Class<?>) t.getRawType();
        return compatible(toClass, raw);
      } else {
        return false;
      }
    } else if (to instanceof TypeVariable<?>) {
      final TypeVariable<?> v = (TypeVariable<?>) to;
      return passed.add(v) && Arrays.stream(v.getBounds()).allMatch(t -> isAssignable(from, t, passed));
    } else if (to instanceof WildcardType) {
      return Arrays.stream(((WildcardType) to).getUpperBounds()).allMatch(t -> isAssignable(from, t, passed));
    } else if (to instanceof ParameterizedType) {
      final ParameterizedType p = (ParameterizedType) to;
      final Class<?> raw = (Class<?>) p.getRawType();
      if (from instanceof Class<?>) {
        return compatible(raw, (Class<?>) from) && of(p.getActualTypeArguments()).allMatch(ALL::equals);
      } else if (from instanceof ParameterizedType) {
        final ParameterizedType t = (ParameterizedType) from;
        if (compatible(raw, (Class<?>) t.getRawType())) {
          final Map<TypeVariable<?>, Type> mapFrom = resolveVars(from);
          final Map<TypeVariable<?>, Type> mapTo = resolveVars(to);
          for (final TypeVariable<?> v : raw.getTypeParameters()) {
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

  public Map<TypeVariable<?>, Type> resolveVars(@Nonnull Type type) {
    final LinkedHashMap<TypeVariable<?>, Type> map = new LinkedHashMap<>();
    resolveVars(type, map);
    return map;
  }

  private void resolveVars(Type type, Map<TypeVariable<?>, Type> map) {
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
        throwError(new IllegalArgumentException("Illegal type: " + type));
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

  @Nonnull
  public Type getClassType(@Nonnull Class<?> type) {
    return new MaridParameterizedType(null, Class.class, (Type) type);
  }

  @Nonnull
  public Type getType(@Nonnull Class<?> type) {
    final TypeVariable<?>[] vars = type.getTypeParameters();
    if (vars.length == 0) {
      return type;
    } else {
      return new MaridParameterizedType(null, type, vars);
    }
  }

  @Nonnull
  public Type evaluate(@Nonnull Consumer<BiConsumer<Type, Type>> callback, @Nonnull Type type) {
    if (Types.isGround(type)) {
      return type;
    } else {
      final TypeEvaluator evaluator = new TypeEvaluator();
      callback.accept(evaluator);
      return evaluator.eval(type);
    }
  }

  @Nonnull
  public Type nct(@Nonnull Type t1, @Nonnull Type t2) {
    if (t1.equals(t2)) {
      return t1;
    } else {
      final Type at1 = getArrayComponentType(t1), at2 = getArrayComponentType(t2);
      if (at1 != null && at2 != null) {
        final Type c = nct(at1, at2);
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

  @Nonnull
  public Stream<? extends Type> types(@Nonnull Type type) {
    final Map<TypeVariable<?>, Type> map = resolveVars(type);
    return Classes.classes(Types.getRaw(type))
        .map(c -> {
          final TypeVariable<?>[] vars = c.getTypeParameters();
          return vars.length == 0 ? c : new MaridParameterizedType(null, c, vars);
        })
        .map(t -> resolve(t, map));
  }

  private final class TypeEvaluator implements BiConsumer<Type, Type> {

    private final HashSet<Type> passed = new HashSet<>();
    private final LinkedHashMap<TypeVariable<?>, LinkedHashSet<Type>> typeMappings = new LinkedHashMap<>();

    @Override
    public void accept(Type formal, Type actual) {
      if (formal instanceof TypeVariable<?>) {
        final TypeVariable<?> typeVariable = (TypeVariable<?>) formal;
        for (final Type bound : typeVariable.getBounds()) {
          accept(bound, actual);
        }
        put(typeVariable, actual);
      } else if (passed.add(formal)) {
        if (Types.isArrayType(formal) && Types.isArrayType(actual)) {
          accept(getArrayComponentType(formal), getArrayComponentType(actual));
        } else if (formal instanceof ParameterizedType) {
          final ParameterizedType p = (ParameterizedType) formal;
          final Map<TypeVariable<?>, Type> map = resolveVars(actual);
          final ParameterizedType resolved = (ParameterizedType) resolve(formal, map);
          final Type[] formals = p.getActualTypeArguments();
          final Type[] actuals = resolved.getActualTypeArguments();
          for (int i = 0; i < formals.length; i++) {
            accept(formals[i], actuals[i]);
          }
        } else if (formal instanceof WildcardType) {
          final WildcardType wildcardType = (WildcardType) formal;
          for (final Type bound : wildcardType.getUpperBounds()) {
            accept(bound, actual);
          }
        }
      }
    }

    @Nonnull
    private Type eval(@Nonnull Type type) {
      final LinkedHashMap<TypeVariable<?>, Type> mapping = new LinkedHashMap<>(typeMappings.size());
      typeMappings.forEach((k, v) -> mapping.put(k, v.stream().reduce(TypeContext.this::nct).orElse(k)));
      return ground(type, mapping);
    }

    private void put(TypeVariable<?> variable, Type type) {
      typeMappings.computeIfAbsent(variable, k -> new LinkedHashSet<>()).add(boxed(type));
    }
  }
}
