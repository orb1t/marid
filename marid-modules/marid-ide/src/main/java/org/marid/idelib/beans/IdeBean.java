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

package org.marid.idelib.beans;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.shape.Circle;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.marid.beans.MaridBean;
import org.marid.expression.mutable.Expr;
import org.marid.expression.mutable.NullExpr;
import org.marid.ide.common.IdeShapes;
import org.marid.jfx.props.FxObject;
import org.marid.jfx.props.ObservablesProvider;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.transform.stream.StreamResult;
import java.io.Writer;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import static javafx.collections.FXCollections.observableArrayList;
import static org.marid.io.Xmls.*;
import static org.marid.jfx.props.ObservablesProvider.object;
import static org.marid.jfx.props.ObservablesProvider.toObservableList;

public class IdeBean implements MaridBean, ObservablesProvider {

  public final IdeBean parent;
  public final StringProperty name;
  public final FxObject<Expr> factory;
  public final ObservableList<IdeBean> children;

  public IdeBean(@Nullable IdeBean parent, @Nonnull String name, @Nonnull Expr factory) {
    this.parent = parent;
    this.name = new SimpleStringProperty(name);
    this.factory = object(factory);
    this.children = observableArrayList(IdeBean::observables);
  }

  public IdeBean() {
    this(null, "beans", new NullExpr());
  }

  public IdeBean(@Nullable IdeBean parent, @Nonnull Element element) {
    this.parent = parent;
    this.name = new SimpleStringProperty(attribute(element, "name").orElse(""));
    this.factory = object(element("factory", element).map(Expr::of).orElseGet(NullExpr::new));
    this.children = elements(element, "bean").map(e -> new IdeBean(this, e)).collect(toObservableList());
  }

  public IdeBean(@Nonnull Element element) {
    this(null, element);
  }

  @Override
  public IdeBean getParent() {
    return parent;
  }

  @Nonnull
  @Override
  public String getName() {
    return name.get();
  }

  @Nonnull
  @Override
  public Expr getFactory() {
    return factory.get();
  }

  @Nonnull
  @Override
  public List<IdeBean> getChildren() {
    return children;
  }

  @SafeVarargs
  public final IdeBean add(@Nonnull String name, @Nonnull Expr factory, @Nonnull Consumer<IdeBean>... consumers) {
    final IdeBean child = new IdeBean(this, name, factory);
    children.add(child);
    for (final Consumer<IdeBean> consumer : consumers) {
      consumer.accept(child);
    }
    return this;
  }

  public void writeTo(@Nonnull Element element) {
    element.setAttribute("name", getName());
    create(element, "factory", f -> create(f, getFactory().getTag(), getFactory()::writeTo));
    children.forEach(c -> create(element, "bean", c::writeTo));
  }

  public void save(@Nonnull Writer writer) {
    writeFormatted("bean", this::writeTo, new StreamResult(writer));
  }

  public void save(@Nonnull Path file) {
    writeFormatted("bean", this::writeTo, file);
  }

  public Circle icon() {
    int hash = 0;
    for (IdeBean b = this; b != null; b = b.parent) {
      hash ^= b.getName().hashCode();
    }
    return IdeShapes.circle(hash, 16);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
