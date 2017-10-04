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

package org.marid.ide.model.expr;

import javafx.beans.Observable;
import javafx.collections.ObservableList;
import org.marid.function.Suppliers;
import org.marid.runtime.expression.Expression;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

import static javafx.collections.FXCollections.observableArrayList;

public abstract class AbstractExpression implements Expression {

    private final Supplier<Observable[]> observables = Suppliers.memoized(this::observables);
    private final ObservableList<AbstractExpression> initializers = observableArrayList(e -> e.observables.get());

    protected Observable[] observables() {
        return new Observable[] {initializers};
    }

    @Nonnull
    @Override
    public ObservableList<AbstractExpression> getInitializers() {
        return initializers;
    }
}
