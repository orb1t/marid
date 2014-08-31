/*
 * Copyright (C) 2014 Dmitry Ovchinnikov
 * Marid, the free data acquisition and visualization software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.marid.bd.blocks.expressions;

import org.codehaus.groovy.ast.expr.*;
import org.marid.bd.StandardBlock;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov
 */
@org.springframework.stereotype.Component
public class TernaryBlock extends StandardBlock {

    protected BooleanExpression expression;
    protected Expression trueExpr;
    protected Expression falseExpr;

    protected final In<BooleanExpression> expInput = new In<>("test", BooleanExpression.class, e -> expression = e);
    protected final In<Expression> trueInput = new In<>("+", Expression.class, e -> trueExpr = e);
    protected final In<Expression> falseInput = new In<>("-", Expression.class, e -> falseExpr = e);

    protected final Out<TernaryExpression> out = new Out<>("out", TernaryExpression.class, () -> new TernaryExpression(expression, trueExpr, falseExpr));

    public TernaryBlock() {
        super("Ternary Expression", "?:", "?:", Color.BLUE);
    }

    @Override
    public void reset() {
        expression = new BooleanExpression(ConstantExpression.TRUE);
        trueExpr = EmptyExpression.INSTANCE;
        falseExpr = EmptyExpression.INSTANCE;
    }

    @Override
    public List<Input<?>> getInputs() {
        return Arrays.asList(expInput, trueInput, falseInput);
    }

    @Override
    public List<Output<?>> getOutputs() {
        return Collections.singletonList(out);
    }
}
