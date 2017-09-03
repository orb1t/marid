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

package org.marid.ide.model;

import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import org.marid.runtime.beans.Bean;
import org.marid.runtime.beans.BeanMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;
import static javafx.collections.FXCollections.observableArrayList;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanData extends BeanMethodData {

    public final BeanData parent;
    public final StringProperty name = new SimpleStringProperty();
    public final StringProperty factory = new SimpleStringProperty();
    public final ObservableList<BeanMethodData> initializers = observableArrayList(BeanMethodData::observables);
    public final ObservableList<BeanData> children = observableArrayList(BeanData::observables);

    public BeanData(@Nullable BeanData parentBean, @Nonnull Bean bean) {
        parent = parentBean;
        name.set(bean.name);
        factory.set(bean.factory);
        signature.set(bean.signature);
        args.setAll(of(bean.args).map(b -> new BeanMethodArgData(this, b)).collect(toList()));
        initializers.setAll(bean.initializers.stream().map(p -> new BeanMethodData(this, p)).collect(toList()));
        children.setAll(bean.children.stream().map(b -> new BeanData(this, b)).collect(toList()));
        name.addListener((o, oldName, newName) -> {
            if (parentBean != null && oldName != null) {
                checkNameUniqueness();
                parentBean.children.filtered(b -> b != this).forEach(b -> b.changeName(oldName, newName));
            }
        });
        if (parentBean != null) {
            checkNameUniqueness();
        }
    }

    public BeanData() {
        this(null, new Bean());
    }

    public BeanData add(BeanMethod... initializers) {
        for (final BeanMethod initializer : initializers) {
            this.initializers.add(new BeanMethodData(this, initializer));
        }
        return this;
    }

    public BeanData add(Bean bean) {
        final BeanData beanData = new BeanData(this, bean);
        children.add(beanData);
        return beanData;
    }

    public String getName() {
        return name.get();
    }

    public String getFactory() {
        return factory.get();
    }

    public Stream<BeanData> parents() {
        return parent == null ? Stream.empty() : concat(of(parent), parent.parents());
    }

    public Stream<BeanData> referents() {
        return concat(children.stream(), parents().flatMap(b -> b.children.stream()))
                .filter(b -> b != this)
                .filter(b -> b.parent != null);
    }

    public Bean toBean() {
        final BeanMethod producer = toMethod();
        final Bean bean = new Bean(getName(), getFactory(), producer.signature, producer.args);
        bean.addInitializers(initializers.stream().map(BeanMethodData::toMethod).collect(toList()));
        bean.addChildren(children.stream().map(BeanData::toBean).collect(toList()));
        return bean;
    }

    public List<BeanMethodArgData> getArgs(int initializer) {
        return initializers.get(initializer).args;
    }

    public Observable[] observables() {
        return new Observable[]{name, factory, signature, args, initializers, children};
    }

    private void checkNameUniqueness() {
        while (parent.children.stream().anyMatch(b -> b != this && b.getName().equals(name.get()))) {
            name.set(name.get() + "_new");
        }
    }

    private void changeName(String oldName, String newName) {
        if (oldName.equals(getFactory())) {
            factory.set(newName);
        }
        final Consumer<BeanMethodArgData> argConsumer = a -> {
            if ("ref".equals(a.getType()) && oldName.equals(a.getValue())) {
                a.value.set(newName);
            }
        };
        args.forEach(argConsumer);
        initializers.forEach(i -> i.args.forEach(argConsumer));
        children.forEach(b -> b.changeName(oldName, newName));
    }

    @Override
    public String toString() {
        return toBean().toString();
    }
}
