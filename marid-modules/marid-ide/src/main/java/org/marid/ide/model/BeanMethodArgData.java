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
import org.marid.runtime.beans.BeanMethodArg;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author Dmitry Ovchinnikov
 */
public final class BeanMethodArgData {

    @Nonnull
    public final BeanMethodData parent;

    public final StringProperty name = new SimpleStringProperty();
    public final StringProperty type = new SimpleStringProperty();
    public final StringProperty filter = new SimpleStringProperty();
    public final StringProperty value = new SimpleStringProperty();

    public BeanMethodArgData(@Nonnull BeanMethodData parent, BeanMethodArg member) {
        this(parent);
        name.set(member.name);
        type.set(member.type);
        filter.set(member.filter);
        value.set(member.value);
    }

    public BeanMethodArgData(@Nonnull BeanMethodData parent) {
        this.parent = parent;
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

    public BeanMethodArg toMember() {
        return new BeanMethodArg(getName(), getType(), getFilter(), getValue());
    }

    public Observable[] observables() {
        return new Observable[] {name, type, filter, value};
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getType(), getFilter(), getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BeanMethodArgData) {
            final BeanMethodArgData that = (BeanMethodArgData) obj;
            return Arrays.equals(
                    new Object[] {this.getName(), this.getType(), this.getFilter(), this.getValue()},
                    new Object[] {that.getName(), that.getType(), that.getFilter(), that.getValue()}
            );
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return toMember().toString();
    }
}
