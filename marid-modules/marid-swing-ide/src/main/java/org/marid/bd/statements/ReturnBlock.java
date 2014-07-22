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

package org.marid.bd.statements;

import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.marid.bd.IoBlock;

import java.awt.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class ReturnBlock extends IoBlock<Expression, ReturnStatement> {

    protected Expression expression;

    public ReturnBlock() {
        super("Return Statement", "ret", "return", Color.GREEN.darker(), Expression.class, ReturnStatement.class);
    }

    @Override
    public void set(Expression value) {
        expression = value;
    }

    @Override
    public void reset() {
        expression = null;
    }

    @Override
    public ReturnStatement get() {
        return new ReturnStatement(expression);
    }
}
