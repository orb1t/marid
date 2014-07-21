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

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.marid.bd.StandardBlock;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.codehaus.groovy.ast.ClassHelper.OBJECT_TYPE;

/**
 * @author Dmitry Ovchinnikov
 */
public class CastBlock extends StandardBlock {

    protected Expression expression;
    protected ClassNode classNode = OBJECT_TYPE;

    protected final Input<Expression> exprInput = in("expr", e -> expression = e, () -> expression = null);
    protected final Input<ClassNode> classInput = in("class", c -> classNode = c, () -> classNode = OBJECT_TYPE);
    protected final Output<CastExpression> castExpr = out("out", () -> new CastExpression(classNode, expression));

    public CastBlock() {
        super("Cast Expression", "(*)", "(*)", Color.BLUE);
    }

    @Override
    public List<Input<?>> getInputs() {
        return Arrays.asList(exprInput, classInput);
    }

    @Override
    public List<Output<?>> getOutputs() {
        return Collections.singletonList(castExpr);
    }
}
