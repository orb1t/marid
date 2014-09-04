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

import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.marid.bd.IoBlock;
import org.marid.bd.blocks.BdBlock;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov
 */
@BdBlock
public class BlockBlock extends IoBlock<Statement, BlockStatement> {

    protected final List<Statement> statements = new ArrayList<>();

    public BlockBlock() {
        super("Block Statement", "{...}", "{...}", Color.GREEN.darker(), Statement.class, BlockStatement.class);
    }

    @Override
    public void set(Statement value) {
        statements.add(value);
    }

    @Override
    public void reset() {
        statements.clear();
    }

    @Override
    public BlockStatement get() {
        return new BlockStatement(statements, new VariableScope());
    }
}
