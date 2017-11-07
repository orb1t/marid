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

package org.marid.expression;

import org.marid.expression.generic.CallExpression;
import org.marid.expression.generic.ClassExpression;
import org.marid.runtime.context.MaridRuntimeUtils;
import org.marid.types.TypeContext;
import org.marid.types.TypeUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Stream;

import static org.marid.types.TypeUtils.WILDCARD;

public interface TypedCallExpression extends CallExpression, TypedExpression {

	@Nonnull
	@Override
	TypedExpression getTarget();

	@Nonnull
	@Override
	List<? extends TypedExpression> getArgs();

	@Nonnull
	@Override
	default Type getType(@Nullable Type owner, @Nonnull TypeContext context) {
		final Type targetType = getTarget().resolveType(owner, context);
		if (getTarget() instanceof ClassExpression) { // static call
			if ("new".equals(getMethod())) { // constructor
				return TypeUtils.classType(targetType)
						.flatMap(tc -> Stream.of(context.getRaw(tc).getConstructors())
								.filter(e -> TypeUtils.matches(this, e, owner, context))
								.findFirst()
								.map(m -> TypeUtils.type(m, getArgs(), owner, context)))
						.orElse(WILDCARD);
			} else { // static method
				return TypeUtils.classType(targetType)
						.flatMap(t -> Stream.of(context.getRaw(t).getMethods())
								.filter(m -> m.getName().equals(getMethod()) && Modifier.isStatic(m.getModifiers()))
								.filter(e -> TypeUtils.matches(this, e, owner, context))
								.findFirst()
								.map(m -> TypeUtils.type(m, getArgs(), owner, context)))
						.orElse(WILDCARD);
			}
		} else { // virtual method
			return Stream.of(context.getRaw(targetType).getMethods())
					.filter(m -> m.getName().equals(getMethod()) && !Modifier.isStatic(m.getModifiers()))
					.filter(e -> TypeUtils.matches(this, e, owner, context))
					.findFirst()
					.map(m -> TypeUtils.type(m, getArgs(), targetType, context))
					.map(type -> context.resolve(targetType, type))
					.orElse(WILDCARD);
		}
	}

	@Nonnull
	@Override
	default Type resolve(@Nonnull Type type, @Nonnull TypeContext context) {
		if (type instanceof Class<?> || !(getTarget() instanceof TypedThisExpression)) {
			return type;
		} else {
			final Type[] ats = getArgs().stream().map(a -> a.resolveType(type, context)).toArray(Type[]::new);
			final Class<?>[] rts = Stream.of(ats).map(context::getRaw).toArray(Class<?>[]::new);
			return MaridRuntimeUtils.accessibleMethods(context.getRaw(type))
					.filter(m -> m.getName().equals(getMethod()))
					.filter(m -> MaridRuntimeUtils.compatible(m, rts))
					.reduce(type, (t, m) -> {
						final Type[] ts = m.getGenericParameterTypes();
						return context.evaluate(e -> {
							for (int i = 0; i < ts.length; i++) {
								e.where(context.resolve(type, ts[i]), ats[i]);
							}
							return e.resolve(t);
						});
					}, (t1, t2) -> t2);
		}
	}
}
