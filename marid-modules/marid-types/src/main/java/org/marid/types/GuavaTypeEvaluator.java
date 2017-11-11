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

import javax.annotation.Nonnull;
import java.lang.reflect.*;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.stream.Stream;

import static com.google.common.reflect.TypeToken.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toCollection;

public class GuavaTypeEvaluator implements TypeEvaluator {

	private final GuavaTypeContext context;
	private final HashSet<TypeToken<?>> passed = new HashSet<>();
	private final LinkedHashMap<TypeToken<?>, LinkedHashSet<TypeToken<?>>> typeMappings = new LinkedHashMap<>();

	GuavaTypeEvaluator(GuavaTypeContext context) {
		this.context = context;
	}

	@Nonnull
	@Override
	public GuavaTypeEvaluator where(Type formal, Type actual) {
		where(of(formal), of(actual));
		return this;
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
	@Override
	public Type resolve(Type type) {
		try {
			return typeMappings.entrySet().stream().reduce(new TypeResolver(), this::where, (r1, r2) -> r2).resolveType(type);
		} finally {
			typeMappings.clear();
			passed.clear();
		}
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
				return TypeToken.of(TypeUtils.genericArrayType(elementType.getType(), context));
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

	private TypeResolver where(TypeResolver resolver, Entry<TypeToken<?>, LinkedHashSet<TypeToken<?>>> entry) {
		return resolver.where(entry.getKey().getType(), commonAncestor(entry.getKey(), entry.getValue()).getType());
	}
}
