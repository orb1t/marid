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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.marid.runtime.beans.BeanMember;
import org.marid.runtime.beans.BeanProducer;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanProducerData {

    public final StringProperty signature = new SimpleStringProperty();
    public final ObservableList<BeanMemberData> args = FXCollections.observableArrayList(BeanMemberData::observables);

    public BeanProducerData(BeanProducer producer) {
        signature.set(producer.signature);
        args.setAll(Stream.of(producer.args).map(BeanMemberData::new).collect(toList()));
    }

    public BeanProducerData() {
    }

    public String getSignature() {
        return signature.get();
    }

    public Observable[] observables() {
        return new Observable[] {signature, args};
    }

    public BeanProducer toProducer() {
        return new BeanProducer(signature.get(), args.stream().map(BeanMemberData::toMember).toArray(BeanMember[]::new));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BeanProducerData) {
            final BeanProducerData that = (BeanProducerData) obj;
            return Arrays.equals(
                    new Object[] {this.getSignature(), this.args},
                    new Object[] {that.getSignature(), that.args}
            );
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSignature(), args);
    }

    @Override
    public String toString() {
        return toProducer().toString();
    }
}
