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

import org.marid.expression.generic.ClassExpression;
import org.marid.expression.generic.GetExpression;
import org.marid.types.TypeContext;
import org.marid.types.TypeUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;

import static org.apache.commons.lang3.reflect.TypeUtils.WILDCARD_ALL;
import static org.marid.runtime.context.MaridRuntimeUtils.accessibleFields;
import static org.marid.types.TypeUtil.classType;

public interface TypedGetExpression extends GetExpression, TypedExpression {

  @Nonnull
  @Override
  TypedExpression getTarget();

  @Nonnull
  @Override
  default Type getType(@Nullable Type owner, @Nonnull TypeContext context) {
    final Type targetType = getTarget().getType(owner, context);
    if (getTarget() instanceof ClassExpression) {
      return classType(targetType).stream().flatMap(t -> accessibleFields(TypeUtil.getRaw(t)))
          .filter(f -> f.getName().equals(getField()))
          .map(f -> context.resolve(owner, f.getGenericType()))
          .findFirst()
          .orElse(WILDCARD_ALL);
    } else {
      return accessibleFields(TypeUtil.getRaw(targetType))
          .filter(f -> f.getName().equals(getField()))
          .map(f -> context.resolve(targetType, f.getGenericType()))
          .map(t -> context.resolve(owner, t))
          .findFirst()
          .orElse(WILDCARD_ALL);
    }
  }
}
