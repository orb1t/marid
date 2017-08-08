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
    public final StringProperty value = new SimpleStringProperty();

    public BeanMethodArgData(@Nonnull BeanMethodData parent, @Nonnull BeanMethodArg member) {
        this.parent = parent;
        this.name.set(member.name);
        this.type.set(member.type);
        this.value.set(member.value);
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

    public BeanMethodArg toArg() {
        return new BeanMethodArg(getName(), getType(), getValue());
    }

    public Observable[] observables() {
        return new Observable[] {name, type, value};
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getType(), getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BeanMethodArgData) {
            final BeanMethodArgData that = (BeanMethodArgData) obj;
            return Arrays.equals(
                    new Object[] {this.getName(), this.getType(), this.getValue()},
                    new Object[] {that.getName(), that.getType(), that.getValue()}
            );
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return toArg().toString();
    }
}
