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

import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;
import org.marid.bd.StandardBlock;
import org.marid.bd.blocks.BdBlock;

import javax.xml.bind.annotation.XmlRootElement;
import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov
 */
@BdBlock
@XmlRootElement
public class TryCatchBlock extends StandardBlock {

    protected Statement tryStatement;
    protected Statement finallyStatement;
    protected CatchStatement[] catchStatements;

    protected final In tryInput = new In("try", Statement.class, s -> tryStatement = s);
    protected final In catchInput = new In("catch", CatchStatement[].class, s -> catchStatements = s);
    protected final In finallyInput = new In("finally", Statement.class, s -> finallyStatement = s);

    protected final Out out = new Out("out", TryCatchStatement.class, () -> {
        final TryCatchStatement statement = new TryCatchStatement(tryStatement, finallyStatement);
        for (final CatchStatement catchStatement : catchStatements) {
            statement.addCatch(catchStatement);
        }
        return statement;
    });

    public TryCatchBlock() {
        super("Try/Catch Block", "t/c", "try/catch", Color.GREEN.darker());
    }

    @Override
    public void reset() {
        tryStatement = EmptyStatement.INSTANCE;
        finallyStatement = EmptyStatement.INSTANCE;
        catchStatements = new CatchStatement[0];
    }

    @Override
    public List<Input> getInputs() {
        return Arrays.asList(tryInput, catchInput, finallyInput);
    }

    @Override
    public List<Output> getOutputs() {
        return Collections.singletonList(out);
    }
}
