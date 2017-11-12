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

import com.google.common.reflect.TypeResolver;
import com.google.common.reflect.TypeToken;
import org.marid.misc.Casts;
import org.marid.types.beans.TypedBean;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.google.common.reflect.TypeToken.of;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toCollection;
import static org.marid.runtime.context.MaridRuntimeUtils.compatible;
import static org.marid.types.TypeUtils.WILDCARD;

public class GuavaTypeContext implements TypeContext {

  private final TypedBean bean;
  private final ClassLoader classLoader;

  public GuavaTypeContext(TypedBean bean, ClassLoader classLoader) {
    this.bean = bean;
    this.classLoader = classLoader;
  }

  @Nonnull
  @Override
  public Type getBeanType(@Nonnull String name) {
    return bean.matchingCandidates()
        .filter(b -> name.equals(b.getName()))
        .filter(TypedBean.class::isInstance)
        .map(TypedBean.class::cast)
        .findFirst()
        .map(b -> b.getFactory().getType(null, new GuavaTypeContext(b, classLoader)))
        .orElse(WILDCARD);
  }

  @Nonnull
  @Override
  public Type resolve(@Nullable Type owner, @Nonnull Type type) {
    return owner == null ? type : TypeToken.of(owner).resolveType(type).getType();
  }

  @Nonnull
  @Override
  public Class<?> getRaw(@Nonnull Type type) {
    return TypeToken.of(type).getRawType();
  }

  @Override
  public boolean isAssignable(@Nonnull Type from, @Nonnull Type to) {
    if (to.equals(from) || Object.class == to) {
      return true;
    } else if (to instanceof Class<?>) {
      return from instanceof Class<?> && compatible((Class<?>) to, (Class<?>) from);
    } else if (to instanceof TypeVariable<?>) {
      return Arrays.stream(((TypeVariable<?>) to).getBounds()).allMatch(t -> isAssignable(from, t));
    } else {
      final TypeToken<?> tTo = TypeToken.of(to);
      final TypeToken<?> tFrom = TypeToken.of(from);
      if (tTo.isArray() && tFrom.isArray()) {
        final Type fromCt = requireNonNull(tFrom.getComponentType()).getType();
        final Type toCt = requireNonNull(tTo.getComponentType()).getType();
        return isAssignable(fromCt, toCt);
      } else {
        return tFrom.isSubtypeOf(tTo);
      }
    }
  }

  @Nonnull
  @Override
  public ClassLoader getClassLoader() {
    return classLoader;
  }

  @Nonnull
  @Override
  public Type getClassType(@Nonnull Class<?> type) {
    final ParameterizedType parameterizedType = (ParameterizedType) getType(Class.class);
    return new TypeResolver()
        .where(parameterizedType.getActualTypeArguments()[0], type)
        .resolveType(parameterizedType);
  }

  @Nonnull
  @Override
  public Type getType(@Nonnull Class<?> type) {
    final TypeToken<?> t = TypeToken.of(type);
    final TypeToken<?> c = t.getSupertype(Casts.cast(type));
    return c.getType();
  }

  @Nonnull
  @Override
  public Type evaluate(@Nonnull Consumer<BiConsumer<Type, Type>> callback, @Nonnull Type type) {
    if (TypeUtils.isGround(type)) {
      return type;
    } else {
      final GuavaTypeEvaluator evaluator = new GuavaTypeEvaluator();
      callback.accept(evaluator);
      return evaluator.resolve(type);
    }
  }

  private final class GuavaTypeEvaluator implements BiConsumer<Type, Type> {

    private final HashSet<TypeToken<?>> passed = new HashSet<>();
    private final LinkedHashMap<TypeToken<?>, LinkedHashSet<TypeToken<?>>> typeMappings = new LinkedHashMap<>();

    @Override
    public void accept(Type formal, Type actual) {
      where(of(formal), of(actual));
    }

    private void where(TypeToken<?> formal, TypeToken<?> actual) {
      if (!(formal.getType() instanceof TypeVariable<?>) && !passed.add(formal)) {
        return;
      }
      if (formal.isArray() && actual.isArray()) {
        where(formal.getComponentType(), actual.getComponentType());
      } else if (formal.getType() instanceof TypeVariable<?>) {
        final TypeVariable<?> typeVariable = (TypeVariable<?>) formal.getType();
        for (final Type bound : typeVariable.getBounds()) {
          where(of(bound), actual);
        }
        typeMappings.computeIfAbsent(formal, k -> new LinkedHashSet<>()).add(actual.wrap());
      } else if (formal.getType() instanceof ParameterizedType) {
        final Class<?> formalRaw = formal.getRawType();
        final Class<?> actualRaw = actual.getRawType();
        if (formalRaw.isAssignableFrom(actualRaw)) {
          final TypeToken<?> superType = actual.getSupertype(Casts.cast(formalRaw));
          final ParameterizedType actualParameterized = (ParameterizedType) superType.getType();
          final ParameterizedType formalParameterized = (ParameterizedType) formal.getType();
          final Type[] actualTypeArgs = actualParameterized.getActualTypeArguments();
          final Type[] formalTypeArgs = formalParameterized.getActualTypeArguments();
          for (int i = 0; i < actualTypeArgs.length; i++) {
            where(of(formalTypeArgs[i]), of(actualTypeArgs[i]));
          }
        }
      } else if (formal.getType() instanceof WildcardType) {
        final WildcardType wildcardType = (WildcardType) formal.getType();
        for (final Type bound : wildcardType.getUpperBounds()) {
          where(of(bound), actual);
        }
      }
    }

    @Nonnull
    Type resolve(Type type) {
      return typeMappings.entrySet().stream().reduce(new TypeResolver(), this::where, (r1, r2) -> r2).resolveType(type);
    }

    private TypeToken<?> commonAncestor(TypeToken<?> formal, LinkedHashSet<TypeToken<?>> actuals) {
      if (actuals.stream().allMatch(t -> ofNullable(t.getComponentType()).filter(v -> !v.isPrimitive()).isPresent())) {
        final LinkedHashSet<TypeToken<?>> elementActuals = actuals.stream()
            .map(TypeToken::getComponentType)
            .collect(toCollection(LinkedHashSet::new));
        final TypeToken<?> elementType = commonAncestor(of(Object.class), elementActuals);
        if (elementType.getType() instanceof Class<?>) {
          return TypeToken.of(Array.newInstance((Class<?>) elementType.getType(), 0).getClass());
        } else {
          return TypeToken.of(TypeUtils.genericArrayType(elementType.getType(), GuavaTypeContext.this));
        }
      } else {
        final TypeToken<?>[][] tokens = actuals.stream()
            .sorted((t1, t2) -> t1.isSubtypeOf(t2) ? -1 : t2.isSubtypeOf(t1) ? +1 : 0)
            .map(TypeToken::getTypes)
            .map(ts -> ts.toArray(new TypeToken<?>[ts.size()]))
            .toArray(TypeToken<?>[][]::new);
        final int max = Stream.of(tokens).mapToInt(a -> a.length).max().orElse(0);
        for (int level = 0; level < max; level++) {
          for (final TypeToken<?>[] token : tokens) {
            if (level < token.length) {
              final TypeToken<?> actual = token[level];
              if (actuals.stream().allMatch(t -> t.isSubtypeOf(actual))) {
                return actual;
              }
            }
          }
        }
        return formal;
      }
    }

    private TypeResolver where(TypeResolver resolver, Map.Entry<TypeToken<?>, LinkedHashSet<TypeToken<?>>> entry) {
      return resolver.where(entry.getKey().getType(), commonAncestor(entry.getKey(), entry.getValue()).getType());
    }
  }
}
