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

package org.marid.expression.runtime;

import org.marid.expression.generic.NullExpression;
import org.marid.expression.xml.XmlExpression;
import org.marid.runtime.context.BeanContext;
import org.w3c.dom.Element;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.lang.reflect.Type;

public final class NullExpr extends Expr implements NullExpression {

  @NotNull
  private final String type;

  public NullExpr() {
    this("void");
  }

  public NullExpr(@NotNull String type) {
    this.type = type;
  }

  NullExpr(Element element) {
    super(element);
    this.type = XmlExpression.type(element);
  }

  @Override
  protected Object execute(@Nullable Object self, @Nullable Type owner, @NotNull BeanContext context) {
    return null;
  }

  @Override
  public String toString() {
    return "null";
  }

  @NotNull
  @Override
  public String getType() {
    return type;
  }
}
