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
import org.marid.runtime.beans.BeanProducer;

import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static javafx.collections.FXCollections.observableArrayList;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanData {

    public final StringProperty name = new SimpleStringProperty();
    public final StringProperty factory = new SimpleStringProperty();
    public final BeanProducerData producer;
    public final ObservableList<BeanProducerData> initializers = observableArrayList(BeanProducerData::observables);

    public BeanData(Bean bean) {
        name.set(bean.name);
        factory.set(bean.factory);
        producer = new BeanProducerData(bean.producer);
        initializers.setAll(Stream.of(bean.initializers).map(BeanProducerData::new).collect(toList()));
    }

    public BeanData() {
        producer = new BeanProducerData();
    }

    public String getName() {
        return name.get();
    }

    public String getFactory() {
        return factory.get();
    }

    public String getSignature() {
        return producer.getSignature();
    }

    public ObservableList<BeanMemberData> getArgs() {
        return producer.args;
    }

    public Bean toInfo() {
        return new Bean(
                name.get(),
                factory.get(),
                producer.toProducer(),
                initializers.stream().map(BeanProducerData::toProducer).toArray(BeanProducer[]::new)
        );
    }

    public Observable[] observables() {
        return new Observable[] {name, factory, producer.signature, producer.args, initializers};
    }

    @Override
    public String toString() {
        return toInfo().toString();
    }
}
