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
import org.marid.runtime.beans.BeanMethod;
import org.marid.runtime.beans.BeanMethodArg;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static javafx.collections.FXCollections.observableArrayList;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanMethodData {

    @Nonnull
    public final BeanData parent;

    public final StringProperty signature = new SimpleStringProperty();
    public final ObservableList<BeanMethodArgData> args = observableArrayList(BeanMethodArgData::observables);

    public BeanMethodData(@Nonnull BeanData parent, @Nonnull BeanMethod producer) {
        this.parent = parent;
        this.signature.set(producer.signature);
        this.args.setAll(Stream.of(producer.args).map(b -> new BeanMethodArgData(this, b)).collect(toList()));
    }

    BeanMethodData() {
        this.parent = (BeanData) this;
    }

    public String getSignature() {
        return signature.get();
    }

    public Observable[] observables() {
        return new Observable[]{signature, args};
    }

    public BeanMethod toMethod() {
        return new BeanMethod(signature.get(), args.stream().map(BeanMethodArgData::toArg).toArray(BeanMethodArg[]::new));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BeanMethodData) {
            final BeanMethodData that = (BeanMethodData) obj;
            return Arrays.equals(
                    new Object[]{this.getSignature(), this.args},
                    new Object[]{that.getSignature(), that.args}
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
        return toMethod().toString();
    }
}
