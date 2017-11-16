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

import org.marid.expression.generic.SetExpression;
import org.marid.types.TypeContext;
import org.marid.types.TypeUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.function.BiConsumer;

public interface TypedSetExpression extends SetExpression, TypedExpression {

  @Nonnull
  @Override
  TypedExpression getTarget();

  @Nonnull
  @Override
  TypedExpression getValue();

  @Nonnull
  @Override
  default Type getType(@Nullable Type owner, @Nonnull TypeContext context) {
    return getTarget().getType(owner, context);
  }

  @Override
  default void resolve(@Nonnull Type type, @Nonnull TypeContext context, @Nonnull BiConsumer<Type, Type> evaluator) {
    for (final Field field : TypeUtil.getRaw(type).getFields()) {
      if (field.getName().equals(getField())) {
        evaluator.accept(context.resolve(type, field.getGenericType()), getValue().getType(type, context));
        return;
      }
    }
  }

  default boolean isValueAssignableFrom(@Nonnull Type type, @Nullable Type owner, @Nonnull TypeContext context) {
    final Type targetType = getTarget().getType(owner, context);
    for (final Field field : TypeUtil.getRaw(targetType).getFields()) {
      if (field.getName().equals(getField())) {
        if (context.isAssignable(type, field.getGenericType())) {
          return true;
        }
      }
    }
    return false;
  }
}
