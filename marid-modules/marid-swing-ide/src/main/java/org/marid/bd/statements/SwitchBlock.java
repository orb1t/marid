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
import org.codehaus.groovy.ast.stmt.CaseStatement;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.marid.bd.StandardBlock;

import java.awt.*;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

/**
 * @author Dmitry Ovchinnikov
 */
public class SwitchBlock extends StandardBlock {

    protected CaseStatement[] caseStatements;
    protected Statement defStatement;
    protected Expression expression;

    protected final Input<Expression> expressionInput = in("expr", Expression.class, e -> expression = e);
    protected final Input<CaseStatement[]> caseInput = in("cases", CaseStatement[].class, s -> caseStatements = s);
    protected final Input<Statement> defaultInput = in("default", Statement.class, s -> defStatement = s);
    protected final Output<SwitchStatement> out = out("out", SwitchStatement.class, () -> {
        final List<CaseStatement> caseStatementList = caseStatements == null ? emptyList() : asList(caseStatements);
        final Statement defaultStatement = defStatement == null ? EmptyStatement.INSTANCE : defStatement;
        return new SwitchStatement(expression, caseStatementList, defaultStatement);
    });

    public SwitchBlock() {
        super("Switch Statement", "switch", "switch", Color.GREEN.darker());
    }

    @Override
    public List<Input<?>> getInputs() {
        return asList(expressionInput, caseInput, defaultInput);
    }

    @Override
    public List<Output<?>> getOutputs() {
        return Collections.singletonList(out);
    }
}
