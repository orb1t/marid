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
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.marid.bd.StandardBlock;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov
 */
public class CatchBlock extends StandardBlock {

    protected Parameter variable;
    protected Statement body;

    protected final In<Parameter> variableInput = new In<>("var", Parameter.class, p -> variable = p);
    protected final In<Statement> bodyInput = new In<>("body", Statement.class, s -> body = s);

    protected final Out<CatchStatement> out = new Out<>("out", CatchStatement.class, () -> new CatchStatement(variable, body));

    public CatchBlock() {
        super("Catch Block", "catch", "catch", Color.GREEN.darker());
    }

    @Override
    public List<Input<?>> getInputs() {
        return Arrays.asList(variableInput, bodyInput);
    }

    @Override
    public List<Output<?>> getOutputs() {
        return Collections.singletonList(out);
    }
}
