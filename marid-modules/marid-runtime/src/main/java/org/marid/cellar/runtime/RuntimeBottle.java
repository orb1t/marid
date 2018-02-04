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

package org.marid.cellar.runtime;

import org.jetbrains.annotations.NotNull;
import org.marid.expression.runtime.Expr;
import org.marid.expression.runtime.NullExpr;
import org.marid.cellar.common.Bottle;
import org.w3c.dom.Element;

import static org.marid.io.Xmls.attribute;
import static org.marid.io.Xmls.element;

public class RuntimeBottle implements Bottle {

  @NotNull
  private final RuntimeRack rack;

  @NotNull
  private final String name;

  @NotNull
  private final Expr factory;

  private RuntimeBottle(@NotNull RuntimeRack rack, @NotNull String name, @NotNull Expr factory) {
    this.rack = rack;
    this.name = name;
    this.factory = factory;
  }

  public RuntimeBottle(@NotNull RuntimeRack rack, @NotNull Element element) {
    this.rack = rack;
    this.name = attribute(element, "name").orElseThrow(() -> new NullPointerException("name"));
    this.factory = element("factory", element).map(Expr::of).orElseGet(NullExpr::new);
  }

  @NotNull
  @Override
  public String getName() {
    return name;
  }

  @NotNull
  @Override
  public Expr getFactory() {
    return factory;
  }

  @NotNull
  @Override
  public RuntimeRack getRack() {
    return rack;
  }

  @Override
  public String toString() {
    return name + "(" + factory + ")";
  }
}
