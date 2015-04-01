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
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.WhileStatement;
import org.marid.bd.BlockColors;
import org.marid.bd.StandardBlock;
import org.marid.bd.blocks.BdBlock;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Dmitry Ovchinnikov
 */
@BdBlock(name = "While Statement", label = "while", color = BlockColors.STATEMENTS_BLOCK_COLOR)
@XmlRootElement
public class WhileBlock extends StandardBlock {

    protected BooleanExpression expression;
    protected Statement body;

    public final In exprInput = new In("test", BooleanExpression.class, e -> expression = e);
    public final In bodyInput = new In("body", Statement.class, s -> body = s);
    public final Out out = new Out("out", WhileStatement.class, () -> new WhileStatement(expression, body));

    @Override
    public void reset() {
        expression = new BooleanExpression(ConstantExpression.PRIM_FALSE);
        body = EmptyStatement.INSTANCE;
    }
}
