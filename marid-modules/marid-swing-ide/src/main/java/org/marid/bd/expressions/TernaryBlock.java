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

package org.marid.bd.expressions;

import images.Images;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.TernaryExpression;
import org.marid.bd.StandardBlock;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov
 */
public class TernaryBlock extends StandardBlock {

    protected BooleanExpression expression;
    protected Expression trueExpr;
    protected Expression falseExpr;

    protected final Input<BooleanExpression> expInput = in("?", e -> expression = e, () -> expression = null);
    protected final Input<Expression> trueInput = in("+", e -> trueExpr = e, () -> trueExpr = null);
    protected final Input<Expression> falseInput = in("-", e -> falseExpr = e, () -> falseExpr = null);
    protected final Output<TernaryExpression> out = out(">", () -> new TernaryExpression(expression, trueExpr, falseExpr));

    public TernaryBlock() {
        super("Ternary Expression", Images.getIconFromText("?:", 32, 32, Color.BLUE, Color.WHITE));
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