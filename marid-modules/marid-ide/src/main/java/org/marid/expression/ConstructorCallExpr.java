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

import javafx.collections.ObservableList;
import org.marid.jfx.props.FxObject;
import org.marid.runtime.expression.ConstructorCallExpression;
import org.marid.runtime.expression.Expression;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

import static javafx.collections.FXCollections.observableArrayList;

public class ConstructorCallExpr extends AbstractExpression implements ConstructorCallExpression {

    public final FxObject<Expression> target = new FxObject<>(AbstractExpression::getObservables);
    public final ObservableList<Expression> args = observableArrayList(AbstractExpression::getObservables);

    public ConstructorCallExpr(@Nonnull AbstractExpression target, @Nonnull AbstractExpression... args) {
        this.target.set(target);
        this.args.addAll(args);
    }

    public ConstructorCallExpr() {
        this(new NullExpr());
    }

    @Nonnull
    @Override
    public Expression getTarget() {
        return target.get();
    }

    @Override
    public void setTarget(@Nonnull Expression target) {
        this.target.set(target);
    }

    @Nonnull
    @Override
    public List<? extends Expression> getArgs() {
        return args;
    }

    @Override
    public void setArgs(@Nonnull Collection<? extends Expression> args) {
        this.args.setAll(args);
    }
}
