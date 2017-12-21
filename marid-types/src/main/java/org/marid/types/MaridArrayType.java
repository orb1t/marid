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

package org.marid.types;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

public class MaridArrayType implements GenericArrayType {

  private final Type componentType;

  public MaridArrayType(Type componentType) {
    this.componentType = componentType;
  }

  @Override
  public Type getGenericComponentType() {
    return componentType;
  }

  @Override
  public int hashCode() {
    return componentType.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return obj == this
        || obj instanceof GenericArrayType && ((GenericArrayType) obj).getGenericComponentType().equals(componentType);
  }

  @Override
  public String toString() {
    if (componentType instanceof WildcardType) {
      final WildcardType t = (WildcardType) componentType;
      if (t.getUpperBounds().length + t.getLowerBounds().length > 1) {
        return "(" + componentType.getTypeName() + ")[]";
      }
    }
    return componentType.getTypeName() + "[]";
  }
}
