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

package org.marid.meta;

import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import org.marid.expression.AbstractExpression;
import org.marid.jfx.props.FxObject;
import org.marid.runtime.model.MaridBean;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.rmi.server.UID;
import java.util.List;
import java.util.function.Consumer;

import static javafx.collections.FXCollections.observableArrayList;
import static org.marid.expression.NullExpr.NULL;

public class MetaBean implements MaridBean {

    public final UID uid = new UID();
    public final MetaBean parent;
    public final StringProperty name = new SimpleStringProperty();
    public final FxObject<AbstractExpression> factory = new FxObject<>(AbstractExpression::getObservables);
    public final ObservableList<MetaBean> children = observableArrayList(MetaBean::observables);

    public MetaBean(@Nullable MetaBean parent, @Nonnull String name, @Nonnull AbstractExpression factory) {
        this.parent = parent;
        this.name.set(name);
        this.factory.set(factory);
    }

    public MetaBean() {
        this(null, "beans", NULL);
    }

    @Override
    public MaridBean getParent() {
        return parent;
    }

    @Nonnull
    @Override
    public String getName() {
        return name.get();
    }

    @Nonnull
    @Override
    public AbstractExpression getFactory() {
        return factory.get();
    }

    @Nonnull
    @Override
    public List<MetaBean> getChildren() {
        return children;
    }

    @SafeVarargs
    public final MetaBean add(@Nonnull String name,
                              @Nonnull AbstractExpression factory,
                              @Nonnull Consumer<MetaBean>... consumers) {
        final MetaBean child = new MetaBean(this, name, factory);
        children.add(child);
        for (final Consumer<MetaBean> consumer : consumers) {
            consumer.accept(child);
        }
        return this;
    }

    private Observable[] observables() {
        return new Observable[]{name, factory, children};
    }
}
