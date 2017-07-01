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

import com.github.javaparser.ast.body.Parameter;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.annotation.Nonnull;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanParameter implements Comparable<BeanParameter> {

    public final ObjectProperty<Parameter> parameter = new SimpleObjectProperty<>(null, "parameter");
    public final StringProperty name = new SimpleStringProperty(null, "name");
    public final StringProperty type = new SimpleStringProperty(null, "type");
    public final StringProperty value = new SimpleStringProperty(null, "value");

    public BeanParameter(Parameter parameter) {
        this.parameter.set(parameter);
        this.name.set(parameter.getName().asString());
        this.type.set(parameter.getType().asString());
    }

    @Override
    public int compareTo(@Nonnull BeanParameter o) {
        return name.get().compareTo(o.name.get());
    }
}
