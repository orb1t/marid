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

import org.marid.expression.generic.Expression;
import org.marid.types.TypeContext;
import org.marid.types.TypeUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.BiConsumer;

public interface TypedExpression extends Expression {

  @Nonnull
  @Override
  List<? extends TypedExpression> getInitializers();

  @Nonnull
  Type getType(@Nullable Type owner, @Nonnull TypeContext context);

  default Type type(@Nullable Type owner, @Nonnull TypeContext context) {
    final Type type = getType(owner, context);
    final Type resolvedType = TypeUtil.resolve(this, type, context);
    return TypeUtil.ground(resolvedType, context);
  }

  default void resolve(@Nonnull Type type, @Nonnull TypeContext context, @Nonnull BiConsumer<Type, Type> evaluator) {
  }
}
