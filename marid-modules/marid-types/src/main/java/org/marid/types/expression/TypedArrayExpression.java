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

package org.marid.types.expression;

import org.marid.expression.generic.ArrayExpression;
import org.marid.types.TypeContext;
import org.marid.types.TypeUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.List;

public interface TypedArrayExpression extends ArrayExpression, TypedExpression {

	@Nonnull
	@Override
	List<? extends TypedExpression> getElements();

	@Nonnull
	@Override
	default Type getType(@Nullable Type owner, @Nonnull TypeContext context) {
		return TypeUtils.getClass(context.getClassLoader(), getElementType())
				.map(elementClass -> {
					if (elementClass.getTypeParameters().length == 0) {
						return Array.newInstance(elementClass, 0).getClass();
					} else {
						final Type t = context.resolve(owner, context.getType(elementClass));
						final Type et = context.evaluate(e -> getElements().forEach(x -> e.where(t, x.getType(owner, context))), t);
						return TypeUtils.genericArrayType(et, context);
					}
				})
				.orElse(TypeUtils.WILDCARD);
	}
}
