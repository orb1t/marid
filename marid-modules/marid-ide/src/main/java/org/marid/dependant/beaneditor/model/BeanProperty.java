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

package org.marid.dependant.beaneditor.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanProperty implements Comparable<BeanProperty> {

    public final StringProperty name = new SimpleStringProperty(null, "name");
    public final StringProperty value = new SimpleStringProperty(null, "value");

    public BeanProperty(String name, String value) {
        this.name.set(name);
        this.value.set(value);
    }

    @Override
    public int compareTo(@NotNull BeanProperty o) {
        return name.get().compareTo(o.name.get());
    }
}
