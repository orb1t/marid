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

import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.EmptyExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.marid.bd.BlockColors;
import org.marid.bd.StandardBlock;
import org.marid.bd.blocks.BdBlock;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Dmitry Ovchinnikov
 */
@BdBlock(name = "For Statement", label = "for", color = BlockColors.STATEMENTS_BLOCK_COLOR)
@XmlRootElement
public class ForBlock extends StandardBlock {

    protected Parameter parameter;
    protected Expression expression;
    protected Statement body;

    public final In paramInput = new In("i", Parameter.class, p -> parameter = p);
    public final In exprInput = new In("expr", Expression.class, e -> expression = e);
    public final In bodyInput = new In("body", Statement.class, s -> body = s);
    public final Out out = new Out("out", ForStatement.class, () -> new ForStatement(parameter, expression, body));

    @Override
    public void reset() {
        parameter = null;
        expression = EmptyExpression.INSTANCE;
        body = EmptyStatement.INSTANCE;
    }
}
