/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2018 MARID software development group
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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jetbrains.annotations.NotNull;
import org.marid.expression.generic.ApplyExpression;
import org.marid.expression.xml.XmlExpression;
import org.w3c.dom.Element;

import java.util.stream.IntStream;

import static java.util.stream.Collectors.toCollection;
import static org.marid.expression.xml.XmlExpression.indices;

public class ApplyExpr extends CallExpr implements ApplyExpression {

  public final StringProperty type;
  public final ObservableList<Integer> indices;

  public ApplyExpr(@NotNull String type, @NotNull Expr target, @NotNull String method, @NotNull int[] indices, @NotNull Expr... args) {
    super(target, method, args);
    this.type = new SimpleStringProperty(type);
    this.indices = IntStream.of(indices).boxed().collect(toCollection(FXCollections::observableArrayList));
    this.args.setAll(args);
  }

  public ApplyExpr(@NotNull Element element) {
    super(element);
    type = new SimpleStringProperty(XmlExpression.type(element));
    indices = IntStream.of(indices(element)).boxed().collect(toCollection(FXCollections::observableArrayList));
  }

  @NotNull
  @Override
  public String getType() {
    return type.get();
  }

  @NotNull
  @Override
  public int[] getIndices() {
    return indices.stream().mapToInt(Integer::intValue).toArray();
  }
}
