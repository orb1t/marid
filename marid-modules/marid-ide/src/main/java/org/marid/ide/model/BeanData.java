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
import org.marid.runtime.beans.BeanMethodArg;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static javafx.collections.FXCollections.observableArrayList;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanData extends BeanMethodData {

    public final StringProperty name = new SimpleStringProperty();
    public final StringProperty factory = new SimpleStringProperty();
    public final ObservableList<BeanMethodData> initializers = observableArrayList(BeanMethodData::observables);

    public BeanData(@Nonnull Bean bean) {
        name.set(bean.name);
        factory.set(bean.factory);
        signature.set(bean.signature);
        args.setAll(Stream.of(bean.args).map(b -> new BeanMethodArgData(this, b)).collect(toList()));
        initializers.setAll(bean.initializers.stream().map(p -> new BeanMethodData(this, p)).collect(toList()));
    }

    public BeanData(@Nonnull String name,
                    @Nonnull String factory,
                    @Nonnull Constructor<?> constructor,
                    @Nonnull BeanMethodArg... args) {
        this(new Bean(name, factory, constructor, args));
    }

    public BeanData(@Nonnull String name,
                    @Nonnull String factory,
                    @Nonnull Method method,
                    @Nonnull BeanMethodArg... args) {
        this(new Bean(name, factory, method, args));
    }

    public BeanData(@Nonnull String name,
                    @Nonnull String factory,
                    @Nonnull Field field,
                    @Nonnull BeanMethodArg... args) {
        this(new Bean(name, factory, field, args));
    }

    public BeanData add(BeanMethod... initializers) {
        for (final BeanMethod initializer : initializers) {
            this.initializers.add(new BeanMethodData(this, initializer));
        }
        return this;
    }

    public String getName() {
        return name.get();
    }

    public String getFactory() {
        return factory.get();
    }

    public Bean toBean() {
        final BeanMethod producer = toMethod();
        return new Bean(
                getName(),
                getFactory(),
                producer.signature,
                producer.args
        ).add(initializers.stream().map(BeanMethodData::toMethod).collect(toList()));
    }

    public List<BeanMethodArgData> getArgs(int initializer) {
        return initializers.get(initializer).args;
    }

    public Observable[] observables() {
        return new Observable[] {name, factory, signature, args, initializers};
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BeanData) {
            final BeanData that = (BeanData) obj;
            return Arrays.equals(
                    new Object[] {this.getName(), this.getFactory(), this.signature, this.args, this.initializers},
                    new Object[] {that.getName(), that.getFactory(), that.signature, that.args, that.initializers}
            );
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getFactory(), getSignature(), args, initializers);
    }

    @Override
    public String toString() {
        return toBean().toString();
    }
}
