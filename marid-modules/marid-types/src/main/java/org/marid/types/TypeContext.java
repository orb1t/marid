/*-
 * #%L
 * marid-ide
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
import org.marid.types.beans.TypedBean;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;
import static org.apache.commons.lang3.reflect.TypeUtils.WILDCARD_ALL;
import static org.apache.commons.lang3.reflect.TypeUtils.getArrayComponentType;
import static org.marid.runtime.context.MaridRuntimeUtils.compatible;
import static org.marid.types.TypeUtil.boxed;

public class TypeContext {

  private final TypedBean bean;
  private final ClassLoader classLoader;

  public TypeContext(TypedBean bean, ClassLoader classLoader) {
    this.bean = bean;
    this.classLoader = classLoader;
  }

  @Nonnull
  public Type getBeanType(@Nonnull String name) {
    return bean.matchingCandidates()
        .filter(b -> name.equals(b.getName()))
        .filter(TypedBean.class::isInstance)
        .map(TypedBean.class::cast)
        .findFirst()
        .map(b -> b.getFactory().type(null, new TypeContext(b, classLoader)))
        .orElse(WILDCARD_ALL);
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

  public boolean isAssignable(@Nonnull Type from, @Nonnull Type to) {
    if (to.equals(from) || Object.class == to) {
      return true;
    } else if (to instanceof Class<?>) {
      return from instanceof Class<?> && compatible((Class<?>) to, (Class<?>) from);
    } else if (to instanceof TypeVariable<?>) {
      return Arrays.stream(((TypeVariable<?>) to).getBounds()).allMatch(t -> isAssignable(from, t));
    } else {
      if (TypeUtils.isArrayType(from) && TypeUtils.isArrayType(to)) {
        final Type fromCt = getArrayComponentType(from);
        final Type toCt = getArrayComponentType(to);
        return isAssignable(fromCt, toCt);
      } else {
        return TypeUtils.isAssignable(from, to);
      }
    }
  }

  @Nonnull
  public ClassLoader getClassLoader() {
    return classLoader;
  }

  @Nonnull
  public Type getClassType(@Nonnull Class<?> type) {
    return TypeUtils.parameterize(Class.class, (Type) type);
  }

  @Nonnull
  public Type getType(@Nonnull Class<?> type) {
    final TypeVariable<?>[] vars = type.getTypeParameters();
    if (vars.length == 0) {
      return type;
    } else {
      return TypeUtils.parameterize(type, Arrays.copyOf(vars, vars.length, Type[].class));
    }
  }

  @Nonnull
  public Type evaluate(@Nonnull Consumer<BiConsumer<Type, Type>> callback, @Nonnull Type type) {
    if (TypeUtil.isGround(type)) {
      return type;
    } else {
      final GuavaTypeEvaluator evaluator = new GuavaTypeEvaluator();
      callback.accept(evaluator);
      return evaluator.resolve(type);
    }
  }

  private final class GuavaTypeEvaluator implements BiConsumer<Type, Type> {

    private final HashSet<Type> passed = new HashSet<>();
    private final LinkedHashMap<TypeVariable<?>, LinkedHashSet<Type>> typeMappings = new LinkedHashMap<>();

    @Override
    public void accept(Type formal, Type actual) {
      if (!(formal instanceof TypeVariable<?>) && !passed.add(formal)) {
        return;
      }
      if (TypeUtils.isArrayType(formal) && TypeUtils.isArrayType(actual)) {
        accept(getArrayComponentType(formal), getArrayComponentType(actual));
      } else if (formal instanceof TypeVariable<?>) {
        final TypeVariable<?> typeVariable = (TypeVariable<?>) formal;
        for (final Type bound : typeVariable.getBounds()) {
          accept(bound, actual);
        }
        typeMappings.computeIfAbsent(typeVariable, k -> new LinkedHashSet<>()).add(boxed(actual));
      } else if (formal instanceof ParameterizedType) {
        final Class<?> formalRaw = TypeUtil.getRaw(formal);
        final Class<?> actualRaw = TypeUtil.getRaw(actual);
        if (formalRaw.isAssignableFrom(actualRaw)) {
          TypeUtils.getTypeArguments(actual, formalRaw).forEach((v, t) -> {
            typeMappings.computeIfAbsent(v, k -> new LinkedHashSet<>()).add(boxed(t));
          });
        }
      } else if (formal instanceof WildcardType) {
        final WildcardType wildcardType = (WildcardType) formal;
        for (final Type bound : wildcardType.getUpperBounds()) {
          accept(bound, actual);
        }
      }
    }

    @Nonnull
    Type resolve(@Nonnull Type type) {
      final Map<TypeVariable<?>, Type> map = typeMappings.entrySet().stream()
          .collect(Collectors.toMap(Entry::getKey, e -> commonAncestor(e.getKey(), e.getValue())));
      return TypeUtils.unrollVariables(map, type);
    }

    private Type commonAncestor(Type formal, LinkedHashSet<Type> actuals) {
      if (actuals.stream().allMatch(TypeContext.this::isNonPrimitiveArray)) {
        final LinkedHashSet<Type> elementActuals = actuals.stream()
            .map(TypeUtils::getArrayComponentType)
            .collect(toCollection(LinkedHashSet::new));
        final Type elementType = commonAncestor(Object.class, elementActuals);
        if (elementType instanceof Class<?>) {
          return Array.newInstance((Class<?>) elementType, 0).getClass();
        } else {
          return TypeUtils.genericArrayType(elementType);
        }
      } else {
        final Type[][] tokens = actuals.stream()
            .sorted((t1, t2) -> TypeUtils.isAssignable(t1, t2) ? -1 : TypeUtils.isAssignable(t2, t1) ? +1 : 0)
            .map(t -> types(t).toArray(Type[]::new))
            .toArray(Type[][]::new);
        final int max = Stream.of(tokens).mapToInt(a -> a.length).max().orElse(0);
        for (int level = 0; level < max; level++) {
          for (final Type[] token : tokens) {
            if (level < token.length) {
              final Type actual = token[level];
              if (actuals.stream().allMatch(t -> TypeUtils.isAssignable(t, actual))) {
                return actual;
              }
            }
          }
        }
        return formal;
      }
    }
  }

  private Stream<? extends Type> types(Type type) {
    final Class<?> raw = TypeUtil.getRaw(type);
    return Stream.concat(MaridRuntimeUtils.superClasses(raw), Stream.of(raw.getInterfaces()))
        .map(c -> generic(c, type));
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
}
