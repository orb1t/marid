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

import org.apache.commons.lang3.reflect.TypeUtils;
import org.marid.runtime.context.MaridRuntimeUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.lang.reflect.Proxy.newProxyInstance;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;
import static org.marid.runtime.context.MaridRuntimeUtils.compatible;
import static org.marid.runtime.context.MaridRuntimeUtils.superClasses;
import static org.marid.types.MaridWildcardType.ALL;
import static org.marid.types.TypeUtil.*;

public class TypeContext {

  protected final ClassLoader classLoader;
  protected final List<Throwable> errors = new ArrayList<>();

  public TypeContext(ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  public List<Throwable> getErrors() {
    return errors;
  }

  @Nonnull
  public Type resolve(@Nullable Type owner, @Nonnull Type type) {
    if (owner == null) {
      return type;
    } else {
      if (owner instanceof ParameterizedType) {
        return type; // replace vars here
      } else {
        return type;
      }
    }
  }

  @Nonnull
  public Class<?> getClass(@Nonnull String name) {
    try {
      return MaridRuntimeUtils.loadClass(name, classLoader, false);
    } catch (Throwable x) {
      errors.add(x);
      return Object.class;
    }
  }

  @Nonnull
  public TypeVariable<?> var(@Nonnull TypeVariable<?> v, @Nonnull Type... newBounds) {
    return ((TypeVariable<?>) newProxyInstance(classLoader, new Class[]{TypeVariable.class}, (proxy, method, args) -> {
      switch (method.getName()) {
        case "getBounds": return newBounds;
        case "toString": return v.getName();
        case "equals": return v.equals(args[0]);
        case "hashCode": return v.hashCode();
        default: return method.invoke(v, args);
      }
    }));
  }

  @Nonnull
  private HashSet<TypeVariable<?>> passed(@Nonnull Map<TypeVariable<?>, Type> map) {
    final HashSet<TypeVariable<?>> passed = new HashSet<>();
    map.forEach((k, v) -> {
      if (v instanceof TypeVariable<?>) { // cyclic reference detection
        for (Type t = v; t instanceof TypeVariable<?>; t = map.get(v)) {
          if (t.equals(k)) {
            passed.add(k);
            break;
          }
        }
      }
    });
    return passed;
  }

  @Nonnull
  public Type ground(@Nonnull Type type, @Nonnull Map<TypeVariable<?>, Type> map) {
    return ground(type, map, passed(map));
  }

  private Type ground(Type type, Map<TypeVariable<?>, Type> map, HashSet<TypeVariable<?>> passed) {
    if (type instanceof Class<?> || isGround(type)) {
      return type;
    } else if (type instanceof GenericArrayType) {
      final GenericArrayType t = (GenericArrayType) type;
      return new MaridArrayType(ground(t.getGenericComponentType(), map));
    } else if (type instanceof ParameterizedType) {
      final ParameterizedType t = (ParameterizedType) type;
      final Type[] types = Stream.of(t.getActualTypeArguments()).map(e -> ground(e, map)).toArray(Type[]::new);
      return new MaridParameterizedType(t.getOwnerType(), t.getRawType(), types);
    } else if (type instanceof WildcardType) {
      return ground(((WildcardType) type).getUpperBounds()[0], map);
    } else if (type instanceof TypeVariable<?>) {
      final TypeVariable<?> v = (TypeVariable<?>) type;
      if (passed.add(v)) {
        return ground(map.getOrDefault(v, v), map, passed);
      } else {
        return Stream.of(v.getBounds())
            .map(e -> ground(e, map, passed))
            .filter(e -> !(e instanceof TypeVariable))
            .findFirst()
            .orElse(Object.class);
      }
    } else {
      return type;
    }
  }

  @Nonnull
  public Type resolve(@Nonnull Type type, @Nonnull Map<TypeVariable<?>, Type> map) {
    if (type instanceof Class<?> || vars(type).stream().noneMatch(map::containsKey)) {
      return type;
    } else if (type instanceof ParameterizedType) {
      final ParameterizedType t = (ParameterizedType) type;
      final Type[] args = of(t.getActualTypeArguments()).map(e -> resolve(e, map)).toArray(Type[]::new);
      return new MaridParameterizedType(t.getOwnerType(), t.getRawType(), args);
    } else if (type instanceof WildcardType) {
      final WildcardType t = (WildcardType) type;
      final Type[] upper = of(t.getUpperBounds()).map(e -> resolve(e, map)).toArray(Type[]::new);
      final Type[] lower = of(t.getLowerBounds()).map(e -> resolve(e, map)).toArray(Type[]::new);
      return new MaridWildcardType(upper, lower);
    } else if (type instanceof GenericArrayType) {
      final GenericArrayType t = (GenericArrayType) type;
      final Type et = resolve(t.getGenericComponentType(), map);
      return et instanceof Class<?> ? Array.newInstance((Class<?>) et, 0).getClass() : new MaridArrayType(et);
    } else if (type instanceof TypeVariable<?>) {
      final TypeVariable<?> v = (TypeVariable<?>) type;
      final Type[] bounds = of(v.getBounds()).map(e -> resolve(e, map)).toArray(Type[]::new);
      return var(v, bounds);
    } else {
      errors.add(new IllegalStateException("Unsupported type: " + type));
      return type;
    }
  }

  public boolean isAssignable(@Nonnull Type from, @Nonnull Type to) {
    return isAssignable(from, to, new HashSet<>());
  }

  private boolean isAssignable(@Nonnull Type from, @Nonnull Type to, @Nonnull HashSet<TypeVariable<?>> passed) {
    if (to.equals(from) || Object.class.equals(to)) {
      return true;
    } else if (TypeUtil.isArrayType(from) && TypeUtil.isArrayType(to)) {
      final Type fromCt = requireNonNull(getArrayComponentType(from));
      final Type toCt = requireNonNull(getArrayComponentType(to));
      return isAssignable(fromCt, toCt);
    } else if (to instanceof Class<?>) {
      final Class<?> toClass = (Class<?>) to;
      if (from instanceof Class<?>) {
        return compatible(toClass, (Class<?>) from);
      } else if (from instanceof ParameterizedType) {
        final ParameterizedType t = (ParameterizedType) from;
        final Class<?> raw = (Class<?>) t.getRawType();
        return compatible(toClass, raw) && of(t.getActualTypeArguments()).allMatch(WildcardType.class::isInstance);
      } else if (from instanceof TypeVariable<?>) {
        final TypeVariable<?> v = (TypeVariable<?>) from;
        return of(v.getBounds()).allMatch(t -> isAssignable(to, t, passed));
      } else if (from instanceof WildcardType) {
        final WildcardType w = (WildcardType) from;
        return of(w.getUpperBounds()).allMatch(t -> isAssignable(to, t, passed));
      } else {
        return false;
      }
    } else if (to instanceof TypeVariable<?>) {
      final TypeVariable<?> v = (TypeVariable<?>) to;
      return passed.add(v) && Arrays.stream(v.getBounds()).allMatch(t -> isAssignable(from, t));
    } else if (to instanceof WildcardType) {
      return Arrays.stream(((WildcardType) to).getUpperBounds()).allMatch(t -> isAssignable(from, t));
    } else {
      errors.add(new IllegalArgumentException("Unknown type: " + to));
      return false;
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
    if (TypeUtil.isGround(type)) {
      return type;
    } else {
      final TypeEvaluator evaluator = new TypeEvaluator();
      callback.accept(evaluator);
      return evaluator.resolve(type);
    }
  }

  @Nonnull
  public Type commonAncestor(@Nonnull Type formal, @Nonnull Collection<Type> actuals) {
    if (actuals.isEmpty()) {
      return formal;
    } else {
      final Supplier<Stream<Type>> as = () -> actuals.stream().filter(t -> !ALL.equals(t));
      if (as.get().allMatch(TypeContext.this::isNonPrimitiveArray)) {
        final Set<Type> aes = as.get().map(TypeUtil::getArrayComponentType).collect(toCollection(LinkedHashSet::new));
        final Type elementType = commonAncestor(Object.class, aes);
        if (elementType instanceof Class<?>) {
          return Array.newInstance((Class<?>) elementType, 0).getClass();
        } else {
          return new MaridArrayType(elementType);
        }
      } else {
        final Type[][] tss = as.get().sorted(this::cmp).map(t -> types(t).toArray(Type[]::new)).toArray(Type[][]::new);
        final int max = of(tss).mapToInt(a -> a.length).max().orElse(0);
        for (int level = 0; level < max; level++) {
          for (final Type[] ts : tss) {
            if (level < ts.length) {
              final Type actual = ts[level];
              if (actuals.stream().allMatch(t -> isAssignable(t, actual))) {
                return actual;
              }
            }
          }
        }
        return formal;
      }
    }
  }

  private int cmp(Type t1, Type t2) {
    return isAssignable(t1, t2) ? -1 : isAssignable(t2, t1) ? +1 : 0;
  }

  private Stream<? extends Type> types(Type type) {
    final Class<?> raw = TypeUtil.getRaw(type);
    return concat(superClasses(raw), of(raw.getInterfaces())).map(c -> generic(c, type));
  }

  private Type generic(Class<?> c, Type type) {
    final TypeVariable<?>[] vars = c.getTypeParameters();
    if (vars.length == 0) {
      return c;
    } else {
      final Map<TypeVariable<?>, Type> map = TypeUtils.getTypeArguments(type, c);
      return TypeUtils.parameterize(c, map);
    }
  }

  private boolean isNonPrimitiveArray(Type type) {
    if (type instanceof GenericArrayType) {
      return true;
    } else if (type instanceof Class<?>) {
      final Class<?> c = (Class<?>) type;
      return c.isArray() && !c.getComponentType().isPrimitive();
    } else {
      return false;
    }
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
        if (TypeUtil.isArrayType(formal) && TypeUtil.isArrayType(actual)) {
          accept(getArrayComponentType(formal), getArrayComponentType(actual));
        } else if (formal instanceof ParameterizedType) {
          final Class<?> formalRaw = TypeUtil.getRaw(formal);
          final Class<?> actualRaw = TypeUtil.getRaw(actual);
          if (formalRaw.isAssignableFrom(actualRaw)) {
            TypeUtils.getTypeArguments(actual, formalRaw).forEach(this::put);
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
    private Type resolve(@Nonnull Type type) {
      final LinkedHashMap<TypeVariable<?>, Type> mapping = new LinkedHashMap<>(typeMappings.size());
      typeMappings.forEach((k, v) -> mapping.put(k, commonAncestor(k, v)));
      return ground(type, mapping);
    }

    private void put(TypeVariable<?> variable, Type type) {
      typeMappings.computeIfAbsent(variable, k -> new LinkedHashSet<>()).add(boxed(type));
    }
  }
}
