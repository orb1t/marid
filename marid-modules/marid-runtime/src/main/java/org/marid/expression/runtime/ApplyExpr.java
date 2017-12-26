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
import org.marid.expression.generic.ApplyExpression;
import org.marid.expression.xml.XmlExpression;
import org.marid.function.ToImmutableList;
import org.marid.runtime.context.BeanContext;
import org.w3c.dom.Element;

import java.lang.reflect.Type;
import java.util.List;

public class ApplyExpr extends Expr implements ApplyExpression {

  @NotNull
  private final Expr target;

  @NotNull
  private final String method;

  @NotNull
  private final String type;

  @NotNull
  private final int[] indices;

  @NotNull
  private final List<Expr> args;

  public ApplyExpr(@NotNull Expr target,
                   @NotNull String method,
                   @NotNull String type,
                   @NotNull int[] indices,
                   @NotNull Expr... args) {
    this.target = target;
    this.method = method;
    this.type = type;
    this.indices = indices;
    this.args = List.of(args);
  }

  ApplyExpr(@NotNull Element element) {
    this.target = XmlExpression.target(element, Expr::of, ClassExpr::new, RefExpr::new);
    this.method = XmlExpression.method(element);
    this.type = XmlExpression.type(element);
    this.indices = XmlExpression.indices(element);
    this.args = XmlExpression.args(element, Expr::of, StringExpr::new, new ToImmutableList<>());
  }

  @NotNull
  @Override
  public Expr getTarget() {
    return target;
  }

  @NotNull
  @Override
  public String getMethod() {
    return method;
  }

  @NotNull
  @Override
  public String getType() {
    return type;
  }

  @NotNull
  @Override
  public List<Expr> getArgs() {
    return args;
  }

  @NotNull
  @Override
  public int[] getIndices() {
    return indices;
  }

  @Override
  protected Object execute(@Nullable Object self, @Nullable Type selfType, @NotNull BeanContext context) {
    return null;
  }
}
