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

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.StringJoiner;

public class MaridWildcardType implements WildcardType {

  private final Type[] upperBounds;
  private final Type[] lowerBounds;

  public MaridWildcardType(Type[] upperBounds, Type[] lowerBounds) {
    this.upperBounds = upperBounds;
    this.lowerBounds = lowerBounds;
  }

  @Override
  public Type[] getUpperBounds() {
    return upperBounds;
  }

  @Override
  public Type[] getLowerBounds() {
    return lowerBounds;
  }

  @Override
  public int hashCode() {
    return Arrays.deepHashCode(new Object[]{upperBounds, lowerBounds});
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof WildcardType) {
      final WildcardType that = (WildcardType) obj;
      return this == that || Arrays.deepEquals(
          new Object[]{this.getUpperBounds(), this.getLowerBounds()},
          new Object[]{that.getUpperBounds(), that.getLowerBounds()}
      );
    } else {
      return false;
    }
  }

  public String toString() {
    final StringBuilder sb = new StringBuilder();
    final Type[] bounds;

    if (lowerBounds.length > 0) {
      sb.append("? super ");
      bounds = lowerBounds;
    } else {
      if (upperBounds.length > 0 && !upperBounds[0].equals(Object.class)) {
        bounds = upperBounds;
        sb.append("? extends ");
      } else {
        return "?";
      }
    }

    final StringJoiner sj = new StringJoiner(" & ");
    for (final Type bound : bounds) {
      sj.add(bound.getTypeName());
    }
    sb.append(sj);

    return sb.toString();
  }

  public static boolean isAll(Type type) {
    if (type instanceof WildcardType) {
      final WildcardType w = (WildcardType) type;
      final Type[] uppers = w.getUpperBounds();
      return uppers.length == 0 || uppers.length == 1 && uppers[0] == Object.class;
    } else {
      return false;
    }
  }
}
