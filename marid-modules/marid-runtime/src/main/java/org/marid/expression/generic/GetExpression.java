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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.stream.Stream;

public interface GetExpression extends Expression {

  @NotNull
  Expression getTarget();

  @NotNull
  String getField();

  @NotNull
  @Override
  default Type getType(@Nullable Type owner, @NotNull BeanTypeContext context) {
    return getTarget().getTargetClass(owner, context)
        .flatMap(c -> Stream.of(c.getFields()))
        .filter(f -> f.getName().equals(getField()))
        .map(f -> {
          final Type result = context.resolve(new Type[0], new Type[0], this, f.getGenericType());
          if (Modifier.isStatic(f.getModifiers())) {
            return result;
          } else {
            final Type targetType = getTarget().getType(owner, context);
            return context.resolve(targetType, result);
          }
        })
        .findFirst()
        .orElseGet(() -> {
          context.throwError(new IllegalStateException(new NoSuchFieldException(getField())));
          return Object.class;
        });
  }
}
