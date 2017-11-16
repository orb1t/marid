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

import org.apache.commons.lang3.reflect.TypeUtils;
import org.marid.runtime.context.MaridRuntimeUtils;
import org.marid.types.TypeContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Array;
import java.lang.reflect.Type;

public interface NullExpression extends Expression {

  @Nonnull
  String getType();

  @Nonnull
  default Class<?> getType(@Nonnull ClassLoader classLoader, boolean init) {
    final String type = getType();
    final String elementType;
    final int dimensions;
    final int index = type.indexOf("[]");
    if (index >= 0) {
      elementType = type.substring(0, index);
      dimensions = (type.length() - elementType.length()) / 2;
    } else {
      elementType = type;
      dimensions = 0;
    }
    final Class<?> et;
    try {
      et = MaridRuntimeUtils.loadClass(elementType, classLoader, init);
    } catch (ClassNotFoundException x) {
      throw new IllegalStateException(x);
    }
    if (dimensions > 0) {
      return Array.newInstance(et, new int[dimensions]).getClass();
    } else {
      return et;
    }
  }

  @Nonnull
  @Override
  default Type getType(@Nullable Type owner, @Nonnull TypeContext context) {
    return TypeUtils.WILDCARD_ALL;
  }
}
