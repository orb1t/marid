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

package org.marid.ide.beans;

import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.marid.runtime.beans.BeanInfo;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanData {

    public final StringProperty name = new SimpleStringProperty();
    public final StringProperty type = new SimpleStringProperty();
    public final StringProperty value = new SimpleStringProperty();
    public final BooleanProperty temporary = new SimpleBooleanProperty();

    public BeanData(BeanInfo beanInfo) {
        name.set(beanInfo.name);

    }

    public String getName() {
        return name.get();
    }

    public String getType() {
        return type.get();
    }

    public String getValue() {
        return value.get();
    }

    public boolean isTemporary() {
        return temporary.get();
    }

    public BeanInfo toInfo() {
        return null;
    }

    public Observable[] observables() {
        return new Observable[] {name, type, value, temporary};
    }

    @Override
    public String toString() {
        return toInfo().toString();
    }
}
