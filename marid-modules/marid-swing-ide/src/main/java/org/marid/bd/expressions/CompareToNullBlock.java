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
import org.codehaus.groovy.transform.sc.transformers.CompareToNullExpression;
import org.marid.bd.IoBlock;

import static java.awt.Color.BLUE;
import static java.awt.Color.WHITE;

/**
 * @author Dmitry Ovchinnikov
 */
public class CompareToNullBlock extends IoBlock<Expression, CompareToNullExpression> {

    protected Expression expression;

    public CompareToNullBlock() {
        super("Compare To null Expression", "", Images.getIconFromText("== null", 32, 32, BLUE, WHITE));
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
    public CompareToNullExpression get() {
        return new CompareToNullExpression(expression, true);
    }
}
