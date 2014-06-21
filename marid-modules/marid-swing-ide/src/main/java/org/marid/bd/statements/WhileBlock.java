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

import images.Images;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.WhileStatement;
import org.marid.bd.StatelessBlock;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov
 */
public class WhileBlock extends StatelessBlock {

    protected BooleanExpression expression;
    protected Statement body;

    protected final Input<BooleanExpression> exprInput = in("?", e -> expression = e, () -> expression = null);
    protected final Input<Statement> bodyInput = in("*", s -> body = s, () -> body = null);
    protected final Output<WhileStatement> out = out(">", () -> new WhileStatement(expression, body));

    public WhileBlock() {
        super("While Statement", Images.getIconFromText("while", 32, 32, Color.GREEN.darker(), Color.WHITE));
    }

    @Override
    public List<Input<?>> getInputs() {
        return Arrays.asList(exprInput, bodyInput);
    }

    @Override
    public List<Output<?>> getOutputs() {
        return Collections.singletonList(out);
    }
}
