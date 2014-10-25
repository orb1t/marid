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

package org.marid.bd.blocks.statements;

import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.marid.bd.StandardBlock;
import org.marid.bd.blocks.BdBlock;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov
 */
@BdBlock(name = "If Statement", label = "if")
@XmlRootElement
public class IfBlock extends StandardBlock {

    protected BooleanExpression expression;
    protected Statement statement;
    protected Statement elseStatement;

    protected final In exprInput = new In("test", BooleanExpression.class, e -> expression = e);
    protected final In statementInput = new In("+", Statement.class, s -> statement = s);
    protected final In elseInput = new In("-", Statement.class, s -> elseStatement = s);
    protected final Out output = new Out("out", IfStatement.class, () -> new IfStatement(expression, statement, elseStatement));

    @Override
    public void reset() {
        expression = new BooleanExpression(ConstantExpression.TRUE);
        statement = EmptyStatement.INSTANCE;
        elseStatement = EmptyStatement.INSTANCE;
    }

    @Override
    public List<Input> getInputs() {
        return Arrays.asList(exprInput, statementInput, elseInput);
    }

    @Override
    public List<Output> getOutputs() {
        return Collections.singletonList(output);
    }
}
