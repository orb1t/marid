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
import org.marid.expression.generic.MappedExpression;
import org.w3c.dom.Element;

import static org.marid.io.Xmls.attribute;

public class MappedExpr implements MappedExpression {

  private final int index;

  @NotNull
  private final Expr value;

  public MappedExpr(int index, @NotNull Expr value) {
    this.index = index;
    this.value = value;
  }

  public MappedExpr(@NotNull Element element) {
    this.index = attribute(element, "index").map(Integer::valueOf).orElse(-1);
    this.value = Expr.of(element);
  }

  @Override
  public int getMappedIndex() {
    return index;
  }

  @NotNull
  @Override
  public Expr getValue() {
    return value;
  }
}
