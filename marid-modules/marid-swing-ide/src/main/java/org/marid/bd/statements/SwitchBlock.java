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
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.CaseStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.marid.bd.StandardBlock;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.codehaus.groovy.ast.stmt.EmptyStatement.INSTANCE;

/**
 * @author Dmitry Ovchinnikov
 */
public class SwitchBlock extends StandardBlock {

    protected final List<CaseStatement> cases = new ArrayList<>();
    protected Statement defStatement = INSTANCE;
    protected Expression expression = null;

    protected final Input<Expression> expressionInput = in("()", e -> expression = e, () -> expression = null);
    protected final Input<CaseStatement> caseInput = in("?", cases::add, cases::clear);
    protected final Input<Statement> defaultInput = in(":", s -> defStatement = s, () -> defStatement = INSTANCE);
    protected final Output<SwitchStatement> out = out(">", () -> new SwitchStatement(expression, cases, defStatement));

    public SwitchBlock() {
        super("Switch Statement", Images.getIconFromText("switch", 32, 32, Color.GREEN.darker(), Color.WHITE));
    }

    @Override
    public List<Input<?>> getInputs() {
        return Arrays.asList(expressionInput, caseInput, defaultInput);
    }

    @Override
    public List<Output<?>> getOutputs() {
        return Collections.singletonList(out);
    }
}
