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

import com.github.javaparser.ast.body.MethodDeclaration;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.marid.ide.model.Annotations;

import javax.annotation.Nonnull;

import static java.util.stream.Collectors.toList;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanFactoryMethod implements Comparable<BeanFactoryMethod> {

    public final ObjectProperty<MethodDeclaration> method = new SimpleObjectProperty<>(null, "method");
    public final StringProperty name = new SimpleStringProperty(null, "name");
    public final StringProperty type = new SimpleStringProperty(null, "type");
    public final BooleanProperty lazy = new SimpleBooleanProperty(null, "lazy");
    public final BooleanProperty prototype = new SimpleBooleanProperty(null, "prototype");
    public final ObservableList<BeanParameter> parameters = FXCollections.observableArrayList();
    public final ObservableList<BeanProperty> properties = FXCollections.observableArrayList();

    public BeanFactoryMethod(MethodDeclaration declaration) {
        method.set(declaration);
        name.set(declaration.getNameAsString());
        type.set(declaration.getType().asString());
        lazy.set(Annotations.isLazy(declaration));
        prototype.set(Annotations.isPrototype(declaration));
        parameters.setAll(declaration.getParameters().stream().map(BeanParameter::new).sorted().collect(toList()));
    }

    @Override
    public int compareTo(@Nonnull BeanFactoryMethod o) {
        return name.get().compareTo(o.name.get());
    }
}
