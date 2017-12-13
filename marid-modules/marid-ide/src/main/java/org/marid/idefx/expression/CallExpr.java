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

package org.marid.idefx.expression;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import org.marid.expression.generic.CallExpression;
import org.marid.expression.xml.XmlExpression;
import org.marid.idefx.beans.IdeBean;
import org.marid.idefx.visitor.Visitor;
import org.marid.jfx.props.FxObject;
import org.w3c.dom.Element;

import org.jetbrains.annotations.NotNull;
import java.util.List;

import static java.util.stream.Collectors.joining;
import static javafx.collections.FXCollections.observableArrayList;
import static org.marid.jfx.props.ObservablesProvider.object;
import static org.marid.jfx.props.ObservablesProvider.toObservableList;

public class CallExpr extends Expr implements CallExpression {

  public final FxObject<Expr> target;
  public final StringProperty method;
  public final ObservableList<Expr> args;

  public CallExpr(@NotNull Expr target, @NotNull String method, @NotNull Expr... args) {
    this.target = new FxObject<>(Expr::observables, target);
    this.method = new SimpleStringProperty(method);
    this.args = observableArrayList(Expr::observables);
    this.args.setAll(args);
  }

  CallExpr(@NotNull Element element) {
    super(element);
    target = object(XmlExpression.target(element, Expr::of, ClassExpr::new, RefExpr::new));
    method = new SimpleStringProperty(XmlExpression.method(element));
    args = XmlExpression.args(element, Expr::of, StringExpr::new, toObservableList());
  }

  @Override
  Expr[] visit(@NotNull IdeBean bean, @NotNull Expr[] parents, @NotNull Visitor visitor) {
    final Expr[] newParents = super.visit(bean, parents, visitor);
    visitor.visit(bean, newParents, getTarget());
    args.forEach(e -> visitor.visit(bean, newParents, e));
    return newParents;
  }

  @NotNull
  @Override
  public Expr getTarget() {
    return target.get();
  }

  @NotNull
  @Override
  public String getMethod() {
    return method.get();
  }

  @NotNull
  @Override
  public List<Expr> getArgs() {
    return args;
  }

  @Override
  public void writeTo(@NotNull Element element) {
    super.writeTo(element);
    XmlExpression.target(element, getTarget());
    XmlExpression.method(element, getMethod());
    XmlExpression.args(element, getArgs());
  }

  @Override
  public String toString() {
    return getArgs().stream().map(Object::toString).collect(joining(",", target + "." + method + "(", ")"));
  }
}
