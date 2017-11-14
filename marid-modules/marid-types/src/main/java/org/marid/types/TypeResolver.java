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

import org.apache.commons.lang3.reflect.TypeUtils;

import javax.annotation.Nonnull;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;

import static org.apache.commons.lang3.reflect.TypeUtils.getArrayComponentType;
import static org.marid.types.TypeUtil.boxed;

public class TypeResolver {

  private final Class<?> type;
  private final HashMap<TypeVariable<?>, HashSet<Type>> mapping = new HashMap<>();

  public TypeResolver(@Nonnull Class<?> type) {
    this.type = type;
  }

  public void where(@Nonnull Type formal, @Nonnull Type actual) {
    where(new HashSet<>(), formal, actual);
  }

  private void where(@Nonnull HashSet<Type> passed, @Nonnull Type formal, @Nonnull Type actual) {
    if (formal instanceof TypeVariable<?>) {
      mapping.computeIfAbsent((TypeVariable<?>) formal, k -> new HashSet<>()).add(actual);
      for (final Type bound : ((TypeVariable<?>) formal).getBounds()) {
        where(passed, bound, actual);
      }
    } else if (passed.add(formal)) {
      if (TypeUtils.isArrayType(formal) && TypeUtils.isArrayType(actual)) {
        where(passed, getArrayComponentType(formal), getArrayComponentType(actual));
      } else if (formal instanceof ParameterizedType) {
        final Class<?> formalRaw = TypeUtil.getRaw(formal);
        final Class<?> actualRaw = TypeUtil.getRaw(actual);
        if (formalRaw.isAssignableFrom(actualRaw)) {
          TypeUtils.getTypeArguments(actual, formalRaw).forEach((v, t) -> {
            final HashSet<Type> types = mapping.computeIfAbsent(v, k -> new LinkedHashSet<>());
            types.add(boxed(t));
          });
        }
      } else if (formal instanceof WildcardType) {
        final WildcardType wildcardType = (WildcardType) formal;
        for (final Type bound : wildcardType.getUpperBounds()) {
          where(passed, bound, actual);
        }
      }
    }
  }
}
