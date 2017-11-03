/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.marid.beans;

import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.marid.expression.mutable.Expr;
import org.marid.expression.mutable.NullExpr;
import org.marid.jfx.props.FxObject;
import org.marid.types.GuavaTypeContext;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.transform.stream.StreamResult;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static javafx.collections.FXCollections.observableArrayList;
import static org.marid.io.Xmls.*;

public class IdeBean implements TypedBean {

    public final IdeBean parent;
    public final StringProperty name;
    public final FxObject<Expr> factory;
    public final ObservableList<IdeBean> children;

    public IdeBean(@Nullable IdeBean parent, @Nonnull String name, @Nonnull Expr factory) {
        this.parent = parent;
        this.name = new SimpleStringProperty(name);
        this.factory = new FxObject<>(Expr::getObservables, factory);
        this.children = observableArrayList(IdeBean::observables);
    }

    public IdeBean() {
        this(null, "beans", new NullExpr());
    }

    public IdeBean(@Nullable IdeBean parent, @Nonnull Element element) {
        this.parent = parent;
        this.name = new SimpleStringProperty(
                attribute(element, "name").orElseThrow(() -> new NullPointerException("name"))
        );
        this.factory = new FxObject<>(
                Expr::getObservables,
                element("factory", element).map(Expr::of).orElseThrow(() -> new NullPointerException("factory"))
        );
        this.children = elements("children", element)
                .map(e -> new IdeBean(this, e))
                .collect(Collectors.toCollection(() -> observableArrayList(IdeBean::observables)));
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

    public Type getType(ClassLoader classLoader, Properties properties) {
        final GuavaTypeContext context = new GuavaTypeContext(this, classLoader, properties);
        return getFactory().getType(null, context);
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

    private Observable[] observables() {
        return new Observable[]{name, factory, children};
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
