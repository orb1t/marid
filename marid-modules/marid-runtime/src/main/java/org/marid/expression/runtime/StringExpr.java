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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.marid.cellar.ExecutionContext;
import org.marid.expression.generic.StringExpression;
import org.marid.expression.xml.XmlExpression;
import org.w3c.dom.Element;

import java.lang.reflect.Type;

public final class StringExpr extends Expr implements StringExpression {

  @NotNull
  private final String value;

  public StringExpr(@NotNull String value) {
    this.value = value;
  }

  StringExpr(@NotNull Element element) {
    super(element);
    value = XmlExpression.string(element);
  }

  @Override
  protected Object execute(@Nullable Object self, @Nullable Type owner, @NotNull ExecutionContext context) {
    return value;
  }

  @NotNull
  @Override
  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return "\"" + value.replace("\"", "\\\"") + "\"";
  }
}
