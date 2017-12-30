/*-
 * #%L
 * marid-ide
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

package org.marid.beans;

import org.marid.expression.generic.Expression;
import org.marid.types.TypeContext;
import org.marid.types.Types;

import org.jetbrains.annotations.NotNull;
import java.lang.reflect.Type;

public abstract class BeanTypeContext extends TypeContext {

  @NotNull
  public abstract MaridBean getBean();

  @NotNull
  public abstract Type getBeanType(@NotNull String name);

  @NotNull
  public Type resolve(@NotNull Type[] formals, @NotNull Type[] actuals, @NotNull Expression expr, @NotNull Type type) {
    if (type instanceof Class<?>) {
      return type;
    } else {
      return Types.evaluate(e -> {
        for (int i = 0; i < formals.length; i++) {
          e.bind(formals[i], actuals[i]);
        }
        for (final Expression i : expr.getInitializers()) {
          i.resolve(type, this, e);
        }
      }, type);
    }
  }
}
