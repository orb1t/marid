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

package org.marid.bd.binary;

import org.marid.bd.components.StandardBlockComponent;

import javax.swing.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class BinExpComponent extends StandardBlockComponent<BinExpBlock> implements BinExpListener {

    protected final JLabel tokenLabel;

    public BinExpComponent(BinExpBlock block) {
        super(block);
        add(tokenLabel = new JLabel(block.getTokenType().icon));
    }

    @Override
    public void changedTokenType(BinExpBlock.TokenType oldType, BinExpBlock.TokenType newType) {
        tokenLabel.setIcon(newType.icon);
    }
}
