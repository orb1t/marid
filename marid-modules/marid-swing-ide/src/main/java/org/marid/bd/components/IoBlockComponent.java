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

package org.marid.bd.components;

import org.marid.bd.Block;
import org.marid.bd.BlockComponent;
import org.marid.bd.IoBlock;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov
 */
public class IoBlockComponent<B extends IoBlock> extends DefaultBlockComponent<B> {

    protected final Port port;

    public IoBlockComponent(B block) {
        super(new BorderLayout(), block);
        add(port = new Port());
    }

    @Override
    public List<Input> getInputs() {
        return Collections.singletonList(port);
    }

    @Override
    public List<Output> getOutputs() {
        return Collections.singletonList(port);
    }

    protected class Port extends JToggleButton implements Input, Output {

        public Port() {
            setIcon(block.getVisualRepresentation());
            setText(block.getButtonName());
        }

        @Override
        public Block.Input<?> getInput() {
            return block;
        }

        @Override
        public Block.Output<?> getOutput() {
            return block;
        }

        @Override
        public BlockComponent getBlockComponent() {
            return IoBlockComponent.this;
        }
    }
}
