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

package org.marid.bd;

import org.marid.bd.expressions.*;
import org.marid.bd.expressions.binary.BinExpBlock;
import org.marid.bd.expressions.constant.ConstantBlock;
import org.marid.bd.statements.IfBlock;
import org.marid.bd.statements.ReturnBlock;

import java.util.function.BiConsumer;

/**
 * @author Dmitry Ovchinnikov
 */
public class DefaultBlockProvider implements BlockProvider {
    @Override
    public void visit(BiConsumer<String, Block> blockConsumer) {
        blockConsumer.accept("Expressions", new ConstantBlock());
        blockConsumer.accept("Expressions", new BinExpBlock());
        blockConsumer.accept("Expressions", new BoolExpBlock());
        blockConsumer.accept("Expressions", new NotExpBlock());
        blockConsumer.accept("Expressions", new TernaryBlock());
        blockConsumer.accept("Expressions", new CastBlock());
        blockConsumer.accept("Expressions", new CompareIdentityBlock());
        blockConsumer.accept("Expressions", new CompareToNullBlock());
        blockConsumer.accept("Expressions", new CompareToNonNullBlock());

        blockConsumer.accept("Statements", new ReturnBlock());
        blockConsumer.accept("Statements", new IfBlock());
    }
}
