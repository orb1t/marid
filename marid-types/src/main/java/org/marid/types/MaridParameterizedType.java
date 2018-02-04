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

package org.marid.types;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.StringJoiner;

public class MaridParameterizedType implements ParameterizedType {

  private final Type ownerType;
  private final Type rawType;
  private final Type[] actualTypeArguments;

  public MaridParameterizedType(Type ownerType, Type rawType, Type... actualTypeArguments) {
    this.rawType = rawType;
    this.ownerType = ownerType;
    this.actualTypeArguments = actualTypeArguments;
  }

  @Override
  public Type[] getActualTypeArguments() {
    return actualTypeArguments;
  }

  @Override
  public Type getRawType() {
    return rawType;
  }

  @Override
  public Type getOwnerType() {
    return ownerType;
  }

  @Override
  public int hashCode() {
    return Arrays.deepHashCode(new Object[]{ownerType, rawType, actualTypeArguments});
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ParameterizedType) {
      final ParameterizedType that = (ParameterizedType) obj;
      return this == that || Arrays.deepEquals(
          new Object[]{this.getOwnerType(), this.getRawType(), this.getActualTypeArguments()},
          new Object[]{that.getOwnerType(), that.getRawType(), that.getActualTypeArguments()}
      );
    } else {
      return false;
    }
  }

  public static MaridParameterizedType withTypes(ParameterizedType parameterizedType, Type... parameters) {
    return new MaridParameterizedType(parameterizedType.getOwnerType(), parameterizedType.getRawType(), parameters);
  }

  public String toString() {
    final StringBuilder sb = new StringBuilder();

    if (ownerType != null) {
      sb.append(ownerType.getTypeName());
      sb.append("$");
      sb.append(((Class<?>) rawType).getSimpleName());
    } else {
      sb.append(rawType.getTypeName());
    }

    if (actualTypeArguments != null) {
      final StringJoiner sj = new StringJoiner(", ", "<", ">").setEmptyValue("");
      for (final Type t : actualTypeArguments) {
        sj.add(t.getTypeName());
      }
      sb.append(sj);
    }

    return sb.toString();
  }
}
