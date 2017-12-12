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
import org.marid.expression.generic.ClassExpression;
import org.marid.expression.xml.XmlExpression;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;

public class ClassExpr extends Expr implements ClassExpression {

  public final StringProperty className;

  public ClassExpr(@Nonnull String className) {
    this.className = new SimpleStringProperty(className);
  }

  ClassExpr(@Nonnull Element element) {
    super(element);
    this.className = new SimpleStringProperty(XmlExpression.className(element));
  }

  @Nonnull
  @Override
  public String getClassName() {
    return className.get();
  }

  @Override
  public void writeTo(@Nonnull Element element) {
    super.writeTo(element);
    XmlExpression.className(element, getClassName());
  }

  @Override
  public String toString() {
    return className.get();
  }
}
