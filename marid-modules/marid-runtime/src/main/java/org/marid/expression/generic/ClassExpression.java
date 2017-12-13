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
import org.marid.types.MaridParameterizedType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.lang.reflect.Type;
import java.util.stream.Stream;

public interface ClassExpression extends Expression {

  @NotNull
  String getClassName();

  @NotNull
  @Override
  default Type getType(@Nullable Type owner, @NotNull BeanTypeContext context) {
    return new MaridParameterizedType(null, Class.class, (Type) context.getClass(getClassName()).orElse(Object.class));
  }

  @NotNull
  @Override
  default Stream<Class<?>> getTargetClass(@Nullable Type owner, @NotNull BeanTypeContext context) {
      return context.getClass(getClassName()).stream();
  }
}
