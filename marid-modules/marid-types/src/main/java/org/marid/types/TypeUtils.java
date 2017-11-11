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

import org.marid.misc.Calls;
import org.marid.runtime.context.MaridRuntimeUtils;
import org.marid.types.expression.TypedCallExpression;
import org.marid.types.expression.TypedExpression;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.*;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Optional.empty;
import static java.util.Optional.of;

public interface TypeUtils {

	WildcardType WILDCARD = Calls.call(() -> {
		final Type pt = Class.class.getMethod("forName", String.class).getGenericReturnType();
		return (WildcardType) ((ParameterizedType) pt).getActualTypeArguments()[0];
	});

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
					e.accept(argTypes[i], args.get(i).getType(owner, context));
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
				final Type at = expr.getArgs().get(i).getType(owner, context);
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

	@Nonnull
	static Type genericArrayType(@Nonnull Type elementType, @Nonnull TypeContext context) {
		final Method toArrayMethod;
		try {
			toArrayMethod = Collection.class.getMethod("toArray", Object[].class);
		} catch (NoSuchMethodException x) {
			throw new IllegalStateException(x);
		}
		final GenericArrayType t = (GenericArrayType) toArrayMethod.getGenericReturnType();
		return context.evaluate(e -> e.accept(t.getGenericComponentType(), elementType), t);
	}

	static boolean isGround(@Nonnull Type type) {
		if (type instanceof TypeVariable<?>) {
			return false;
		} else if (type instanceof GenericArrayType) {
			return isGround(((GenericArrayType) type).getGenericComponentType());
		} else if (type instanceof WildcardType) {
			final WildcardType wt = (WildcardType) type;
			final Predicate<Type[]> ground = ts -> Stream.of(ts).allMatch(TypeUtils::isGround);
			return ground.test(wt.getUpperBounds()) && ground.test(wt.getLowerBounds());
		} else if (type instanceof ParameterizedType) {
			return Stream.of(((ParameterizedType) type).getActualTypeArguments()).allMatch(TypeUtils::isGround);
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
			final Function<Type[], Stream<TypeVariable<?>>> vars = ts -> Stream.of(ts).flatMap(TypeUtils::vars);
			return Stream.concat(vars.apply(wt.getUpperBounds()), vars.apply(wt.getLowerBounds()));
		} else if (type instanceof ParameterizedType) {
			return Stream.of(((ParameterizedType) type).getActualTypeArguments()).flatMap(TypeUtils::vars);
		} else {
			return Stream.empty();
		}
	}

	@Nonnull
	static Type ground(@Nonnull Type type, @Nonnull TypeContext context) {
		return context.evaluate(e -> vars(type).forEach(t -> e.accept(t, WILDCARD)), type);
	}
}
