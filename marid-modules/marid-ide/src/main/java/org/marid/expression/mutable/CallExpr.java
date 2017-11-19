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

package org.marid.expression.mutable;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import org.marid.expression.generic.CallExpression;
import org.marid.jfx.props.FxObject;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import java.util.List;

import static java.util.stream.Collectors.toCollection;
import static javafx.collections.FXCollections.observableArrayList;
import static org.marid.io.Xmls.*;

public class CallExpr extends Expr implements CallExpression {

  public final FxObject<Expr> target;
  public final StringProperty method;
  public final ObservableList<Expr> args;

  public CallExpr(@Nonnull Expr target, @Nonnull String method, @Nonnull Expr... args) {
    this.target = new FxObject<>(Expr::getObservables, target);
    this.method = new SimpleStringProperty(method);
    this.args = observableArrayList(Expr::getObservables);
    this.args.setAll(args);
  }

  CallExpr(@Nonnull Element element) {
    super(element);
    target = new FxObject<>(
        Expr::getObservables,
        element("target", element).map(Expr::of).orElseThrow(() -> new NullPointerException("target"))
    );
    method = new SimpleStringProperty(
        attribute(element, "method").orElseThrow(() -> new NullPointerException("method"))
    );
    args = elements("args", element)
        .map(Expr::of)
        .collect(toCollection(() -> observableArrayList(Expr::getObservables)));
  }

  @Nonnull
  @Override
  public Expr getTarget() {
    return target.get();
  }

  @Nonnull
  @Override
  public String getMethod() {
    return method.get();
  }

  @Nonnull
  @Override
  public List<Expr> getArgs() {
    return args;
  }

  @Override
  public void writeTo(@Nonnull Element element) {
    super.writeTo(element);
    create(element, "target", t -> create(t, getTarget().getTag(), getTarget()::writeTo));
    element.setAttribute("method", getMethod());
    if (!args.isEmpty()) {
      create(element, "args", as -> getArgs().forEach(a -> create(as, a.getTag(), a::writeTo)));
    }
  }
}
