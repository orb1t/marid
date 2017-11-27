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

import org.marid.annotation.MetaInfo;
import org.marid.beans.BeanTypeContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Array;
import java.lang.reflect.Type;

@MetaInfo(name = "Null", icon = "D_NULL")
public interface NullExpression extends Expression {

  @Nonnull
  String getType();

  @Nonnull
  @Override
  default Type getType(@Nullable Type owner, @Nonnull BeanTypeContext context) {
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
    return context.getClass(elementType)
        .map(c -> {
          switch (dimensions) {
            case 0:
              return c;
            case 1:
              return Array.newInstance(c, 0).getClass();
            default:
              return Array.newInstance(c, new int[dimensions]).getClass();
          }
        })
        .orElse(Object.class);
  }
}
