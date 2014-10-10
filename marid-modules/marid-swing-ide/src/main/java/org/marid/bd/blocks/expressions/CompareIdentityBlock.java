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

import org.codehaus.groovy.ast.expr.EmptyExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.transform.sc.transformers.CompareIdentityExpression;
import org.marid.bd.StandardBlock;
import org.marid.bd.blocks.BdBlock;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov
 */
@BdBlock
public class CompareIdentityBlock extends StandardBlock {

    protected Expression left;
    protected Expression right;

    protected final In leftInput = new In("expr1", Expression.class, e -> left = e);
    protected final In rightInput = new In("expr2", Expression.class, e -> right = e);
    protected final Out out = new Out("out", CompareIdentityExpression.class, () -> new CompareIdentityExpression(left, right));

    public CompareIdentityBlock() {
        super("Compare Identity Expression", "===", "===", Color.BLUE);
    }

    @Override
    public void reset() {
        left = EmptyExpression.INSTANCE;
        right = EmptyExpression.INSTANCE;
    }

    @Override
    public List<Input> getInputs() {
        return Arrays.asList(leftInput, rightInput);
    }

    @Override
    public List<Output> getOutputs() {
        return Collections.singletonList(out);
    }
}
