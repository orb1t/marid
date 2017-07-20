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
import org.marid.runtime.beans.BeanMember;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanMemberData {

    public final StringProperty name = new SimpleStringProperty();
    public final StringProperty type = new SimpleStringProperty();
    public final StringProperty filter = new SimpleStringProperty();
    public final StringProperty value = new SimpleStringProperty();

    public BeanMemberData(BeanMember member) {
        name.set(member.name);
        type.set(member.type);
        filter.set(member.filter);
        value.set(member.value);
    }

    public BeanMemberData() {
    }

    public String getType() {
        return type.get();
    }

    public String getName() {
        return name.get();
    }

    public String getValue() {
        return value.get();
    }

    public String getFilter() {
        return filter.get();
    }

    public BeanMember toMember() {
        return new BeanMember(getName(), getType(), getFilter(), getValue());
    }

    public Observable[] observables() {
        return new Observable[] {name, type, filter, value};
    }
}
