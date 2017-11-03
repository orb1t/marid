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

package org.marid.expression.mutable;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import org.marid.expression.TypedCallExpression;
import org.marid.jfx.props.FxObject;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import java.util.List;

import static java.util.stream.Collectors.toCollection;
import static javafx.collections.FXCollections.observableArrayList;
import static org.marid.io.Xmls.*;

public class CallExpr extends Expr implements TypedCallExpression {

    public final FxObject<Expr> target;
    public final StringProperty method;
    public final ObservableList<Expr> args;

    public CallExpr(@Nonnull Expr target, @Nonnull String method, @Nonnull Expr... args) {
        this.target = new FxObject<>(Expr::getObservables, target);
        this.method = new SimpleStringProperty(method);
        this.args = observableArrayList(Expr::getObservables);
        this.args.setAll(args);
    }

    CallExpr(@Nonnull Element element) {
        super(element);
        target = new FxObject<>(
                Expr::getObservables,
                element("target", element).map(Expr::of).orElseThrow(() -> new NullPointerException("target"))
        );
        method = new SimpleStringProperty(
                attribute(element, "method").orElseThrow(() -> new NullPointerException("method"))
        );
        args = elements("args", element)
                .map(Expr::of)
                .collect(toCollection(() -> observableArrayList(Expr::getObservables)));
    }

    @Nonnull
    @Override
    public Expr getTarget() {
        return target.get();
    }

    @Nonnull
    @Override
    public String getMethod() {
        return method.get();
    }

    @Nonnull
    @Override
    public List<Expr> getArgs() {
        return args;
    }

    @Override
    public void writeTo(@Nonnull Element element) {
        super.writeTo(element);
        create(element, "target", t -> create(t, getTarget().getTag(), getTarget()::writeTo));
        element.setAttribute("method", getMethod());
        if (!args.isEmpty()) {
            create(element, "args", as -> getArgs().forEach(a -> create(as, a.getTag(), a::writeTo)));
        }
    }
}
