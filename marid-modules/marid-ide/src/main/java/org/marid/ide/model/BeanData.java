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
import org.marid.runtime.beans.BeanMember;

import java.util.stream.Stream;

import static javafx.collections.FXCollections.observableArrayList;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanData {

    public final StringProperty name = new SimpleStringProperty();
    public final StringProperty factory = new SimpleStringProperty();
    public final StringProperty producer = new SimpleStringProperty();
    public final ObservableList<BeanMemberData> args = observableArrayList(BeanMemberData::observables);
    public final ObservableList<BeanMemberData> props = observableArrayList(BeanMemberData::observables);

    public BeanData(Bean bean) {
        name.set(bean.name);
        factory.set(bean.factory);
        producer.set(bean.producer);
        args.setAll(Stream.of(bean.args).map(BeanMemberData::new).toArray(BeanMemberData[]::new));
        props.setAll(Stream.of(bean.props).map(BeanMemberData::new).toArray(BeanMemberData[]::new));
    }

    public String getName() {
        return name.get();
    }

    public String getFactory() {
        return factory.get();
    }

    public String getProducer() {
        return producer.get();
    }

    public Bean toInfo() {
        return new Bean(
                name.get(),
                factory.get(),
                producer.get(),
                args.stream().map(BeanMemberData::toMember).toArray(BeanMember[]::new),
                props.stream().map(BeanMemberData::toMember).toArray(BeanMember[]::new)
        );
    }

    public Observable[] observables() {
        return new Observable[] {name, factory, producer, args, props};
    }

    @Override
    public String toString() {
        return toInfo().toString();
    }
}
