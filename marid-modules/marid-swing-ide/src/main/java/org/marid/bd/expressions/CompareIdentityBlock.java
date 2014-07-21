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
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.transform.sc.transformers.CompareIdentityExpression;
import org.marid.bd.StandardBlock;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov
 */
public class CompareIdentityBlock extends StandardBlock {

    protected Expression left;
    protected Expression right;

    protected final Input<Expression> leftInput = in("expr1", e -> left = e, () -> left = null);
    protected final Input<Expression> rightInput = in("expr2", e -> right = e, () -> right = null);
    protected final Output<CompareIdentityExpression> out = out("out", () -> new CompareIdentityExpression(left, right));

    public CompareIdentityBlock() {
        super("Compare Identity Expression", "===", "===", Color.BLUE);
    }

    @Override
    public List<Input<?>> getInputs() {
        return Arrays.asList(leftInput, rightInput);
    }

    @Override
    public List<Output<?>> getOutputs() {
        return Collections.singletonList(out);
    }
}
