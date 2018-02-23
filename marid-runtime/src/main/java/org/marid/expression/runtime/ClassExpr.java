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
import org.marid.expression.generic.ClassExpression;
import org.marid.expression.xml.XmlExpression;
import org.w3c.dom.Element;

import java.lang.reflect.Type;

public final class ClassExpr extends Expr implements ClassExpression {

  @NotNull
  private final String className;

  public ClassExpr(@NotNull String className) {
    this.className = className;
  }

  ClassExpr(@NotNull Element element) {
    super(element);
    className = XmlExpression.className(element);
  }

  @Override
  protected Class<?> execute(@Nullable Object self, @Nullable Type owner, @NotNull ExecutionContext context) {
    return getTargetClass(owner, context).findFirst().orElse(Object.class);
  }

  @NotNull
  @Override
  public String getClassName() {
    return className;
  }

  @Override
  public String toString() {
    return "&" + className;
  }
}
