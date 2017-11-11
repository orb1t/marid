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
					e.where(argTypes[i], args.get(i).getType(owner, context));
				}
				return e.resolve(returnType);
			});
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
			return context.evaluate(e -> {
				expression.getInitializers().forEach(i -> i.resolve(type, context, e));
				return e.resolve(type);
			});
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
		return context.evaluate(e -> e.where(t.getGenericComponentType(), elementType).resolve(t));
	}
}
