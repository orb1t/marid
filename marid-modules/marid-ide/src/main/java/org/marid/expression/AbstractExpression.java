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

package org.marid.expression;

import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import org.marid.misc.Calls;
import org.marid.runtime.expression.Expression;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.Arrays.deepEquals;
import static javafx.collections.FXCollections.observableArrayList;

public abstract class AbstractExpression implements Expression {

    public final ObservableList<Expression> initializers = observableArrayList(AbstractExpression::getObservables);

    public static Observable[] getObservables(Expression expression) {
        return expression instanceof AbstractExpression
                ? ((AbstractExpression) expression).ostream().toArray(Observable[]::new)
                : new Observable[0];
    }

    @Nonnull
    @Override
    public ObservableList<Expression> getInitializers() {
        return initializers;
    }

    @Override
    public void setInitializers(@Nonnull Collection<? extends Expression> initializers) {
        this.initializers.setAll(initializers);
    }

    private Stream<Observable> ostream() {
        return Stream.of(getClass().getFields())
                .filter(f -> Observable.class.isAssignableFrom(f.getType()))
                .sorted(Comparator.comparing(Field::getName))
                .map(f -> Calls.call(() -> (Observable) f.get(this)));
    }

    private Stream<Object> stream() {
        return ostream().map(v -> v instanceof ObservableValue ? ((ObservableValue) v).getValue() : v);
    }

    @Override
    public int hashCode() {
        return stream().mapToInt(Objects::hashCode).reduce(0, (a, e) -> 31 * a + e);
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null
                && obj.getClass() == getClass()
                && deepEquals(stream().toArray(), ((AbstractExpression) obj).stream().toArray());
    }
}
