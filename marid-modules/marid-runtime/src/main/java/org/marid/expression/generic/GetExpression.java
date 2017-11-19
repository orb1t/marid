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

package org.marid.expression.generic;

import org.marid.beans.BeanTypeContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

public interface GetExpression extends Expression {

  @Nonnull
  Expression getTarget();

  @Nonnull
  String getField();

  @Nonnull
  @Override
  default Type getType(@Nullable Type owner, @Nonnull BeanTypeContext context) {
    final Class<?> targetClass = getTarget().getTargetClass(owner, context);
    try {
      final Field field = targetClass.getField(getField());
      final Type result = context.resolve(new Type[0], new Type[0], this, field.getGenericType());
      if (Modifier.isStatic(field.getModifiers())) {
        return result;
      } else {
        final Type targetType = getTarget().getType(owner, context);
        return context.resolve(targetType, result);
      }
    } catch (NoSuchFieldException x) {
      context.throwError(new IllegalStateException(x));
      return Object.class;
    }
  }
}
