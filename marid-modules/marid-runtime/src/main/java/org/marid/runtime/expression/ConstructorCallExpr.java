/*-
 * #%L
 * marid-runtime
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.marid.runtime.expression;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.joining;
import static org.marid.runtime.expression.NullExpr.NULL;

public class ConstructorCallExpr extends AbstractExpression implements ConstructorCallExpression {

    @Nonnull
    private Expression target;

    @Nonnull
    private final List<Expression> args;

    public ConstructorCallExpr(@Nonnull Expression target, @Nonnull Expression... args) {
        this.target = target;
        this.args = new ArrayList<>(Arrays.asList(args));
    }

    public ConstructorCallExpr() {
        target = NULL;
        args = new ArrayList<>();
    }

    @Override
    @Nonnull
    public Expression getTarget() {
        return target;
    }

    @Override
    public void setTarget(@Nonnull Expression target) {
        this.target = target;
    }

    @Override
    @Nonnull
    public List<Expression> getArgs() {
        return args;
    }

    @Override
    public void setArgs(@Nonnull Collection<? extends Expression> args) {
        this.args.clear();
        this.args.addAll(args);
    }

    @Override
    public String toString() {
        return args.stream().map(Object::toString).collect(joining(",", target + "(", ")"));
    }
}
