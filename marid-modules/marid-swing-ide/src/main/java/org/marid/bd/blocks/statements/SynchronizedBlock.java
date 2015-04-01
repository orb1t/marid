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

import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.SynchronizedStatement;
import org.marid.bd.BlockColors;
import org.marid.bd.StandardBlock;
import org.marid.bd.blocks.BdBlock;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Dmitry Ovchinnikov
 */
@BdBlock(name = "Synchronized Statement", label = "sync", color = BlockColors.STATEMENTS_BLOCK_COLOR)
@XmlRootElement
public class SynchronizedBlock extends StandardBlock {

    protected Expression expression;
    protected Statement statement;

    public final In exprInput = new In("mon", Expression.class, e -> expression = e);
    public final In stmtInput = new In("body", Statement.class, s -> statement = s);
    public final Out out = new Out("out", SynchronizedStatement.class, () -> new SynchronizedStatement(expression, statement));

    @Override
    public void reset() {
        expression = null;
        statement = EmptyStatement.INSTANCE;
    }
}
