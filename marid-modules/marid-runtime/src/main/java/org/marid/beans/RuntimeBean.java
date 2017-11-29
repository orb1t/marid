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

package org.marid.beans;

import org.marid.expression.runtime.Expr;
import org.marid.expression.runtime.NullExpr;
import org.marid.function.ToImmutableList;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Stream;

import static org.marid.io.Xmls.*;

public class RuntimeBean implements MaridBean {

  private final RuntimeBean parent;
  private final String name;
  private final Expr factory;
  private final List<RuntimeBean> children;

  private RuntimeBean(RuntimeBean parent, @Nonnull String name, @Nonnull Expr factory, @Nonnull RuntimeBean... children) {
    this.parent = parent;
    this.name = name;
    this.factory = factory;
    this.children = Stream.of(children)
        .map(o -> new RuntimeBean(this, o.name, o.factory, o.children.toArray(new RuntimeBean[o.children.size()])))
        .collect(new ToImmutableList<>());
  }

  public RuntimeBean(@Nonnull RuntimeBean... children) {
    this("beans", new NullExpr(), children);
  }

  public RuntimeBean(@Nonnull String name, @Nonnull Expr factory, @Nonnull RuntimeBean... children) {
    this(null, name, factory, children);
  }

  public RuntimeBean(RuntimeBean parent, @Nonnull Element element) {
    this.parent = parent;
    this.name = attribute(element, "name").orElseThrow(() -> new NullPointerException("name"));
    this.factory = element("factory", element).map(Expr::of).orElseGet(NullExpr::new);
    this.children = elements(element, "bean").map(e -> new RuntimeBean(this, e)).collect(new ToImmutableList<>());
  }

  @Override
  public RuntimeBean getParent() {
    return parent;
  }

  @Nonnull
  @Override
  public String getName() {
    return name;
  }

  @Nonnull
  @Override
  public Expr getFactory() {
    return factory;
  }

  @Nonnull
  @Override
  public List<RuntimeBean> getChildren() {
    return children;
  }

  @Override
  public String toString() {
    if (children.isEmpty()) {
      return name + "(" + factory + ")";
    } else {
      return name + "(" + factory + ")" + children;
    }
  }
}
