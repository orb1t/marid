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

import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.marid.bd.StandardBlock;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov
 */
public class ForBlock extends StandardBlock {

    protected Parameter parameter;
    protected Expression expression;
    protected Statement body;

    protected final Input<Parameter> paramInput = in("i", p -> parameter = p, () -> parameter = null);
    protected final Input<Expression> exprInput = in("expr", e -> expression = e, () -> expression = null);
    protected final Input<Statement> bodyInput = in("body", s -> body = s, () -> body = null);
    protected final Output<ForStatement> out = out("out", () -> new ForStatement(parameter, expression, body));

    public ForBlock() {
        super("For Statement", "for", "for", Color.GREEN.darker());
    }

    @Override
    public List<Input<?>> getInputs() {
        return Arrays.asList(paramInput, exprInput, bodyInput);
    }

    @Override
    public List<Output<?>> getOutputs() {
        return Collections.singletonList(out);
    }
}
