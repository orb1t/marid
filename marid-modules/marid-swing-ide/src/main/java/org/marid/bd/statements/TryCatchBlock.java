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
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;
import org.marid.bd.StatelessBlock;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov
 */
public class TryCatchBlock extends StatelessBlock {

    protected Statement tryStatement;
    protected Statement body;

    protected final Input<Statement> tryInput = in("t", s -> tryStatement = s, () -> tryStatement = null);
    protected final Input<Statement> bodyInput = in("c", s -> body = s, () -> body = null);
    protected final Output<TryCatchStatement> out = out(">", () -> new TryCatchStatement(tryStatement, body));

    public TryCatchBlock() {
        super("Try/Catch Block", Images.getIconFromText("t/c", 32, 32, Color.GREEN.darker(), Color.WHITE));
    }

    @Override
    public List<Input<?>> getInputs() {
        return Arrays.asList(tryInput, bodyInput);
    }

    @Override
    public List<Output<?>> getOutputs() {
        return Collections.singletonList(out);
    }
}
