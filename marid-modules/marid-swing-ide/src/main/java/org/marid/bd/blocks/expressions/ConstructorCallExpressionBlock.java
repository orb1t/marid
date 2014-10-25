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

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.marid.bd.StandardBlock;
import org.marid.bd.blocks.BdBlock;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov
 */
@BdBlock(name = "Constructor Call Expression", label = "X(...)")
@XmlRootElement
public class ConstructorCallExpressionBlock extends StandardBlock {

    protected ClassNode classNode;
    protected Expression args;

    protected final In classIn = new In("class", ClassNode.class, true, v -> classNode = v);
    protected final In argsIn = new In("args", Expression.class, v -> args = v);

    protected final Out out = new Out("cc", ConstructorCallExpression.class, this::value);

    protected ConstructorCallExpression value() {
        return new ConstructorCallExpression(classNode, args);
    }

    @Override
    public void reset() {
        args = new ArgumentListExpression(new Parameter[0]);
    }

    @Override
    public List<Input> getInputs() {
        return Arrays.asList(classIn, argsIn);
    }

    @Override
    public List<Output> getOutputs() {
        return Collections.singletonList(out);
    }
}
