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

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov
 */
public class StandardBlockComponent<B extends Block> extends DefaultBlockComponent<B> {

    protected final List<Input> inputs = new ArrayList<>();
    protected final List<Output> outputs = new ArrayList<>();

    public StandardBlockComponent(B block) {
        super(new BorderLayout(), block);
        final JPanel inputsPanel = new JPanel(new GridLayout(0, 1));
        final JPanel outputsPanel = new JPanel(new GridLayout(0, 1));
        inputsPanel.setOpaque(false);
        outputsPanel.setOpaque(false);
        add(inputsPanel, BorderLayout.WEST);
        add(outputsPanel, BorderLayout.EAST);
        for (final Block.Input<?> in : block.getInputs()) {
            final DefaultInput input = new DefaultInput(in);
            inputs.add(input);
            inputsPanel.add(input);

        }
        for (final Block.Output<?> out : block.getOutputs()) {
            final DefaultOutput output = new DefaultOutput(out);
            outputs.add(output);
            outputsPanel.add(output);
        }
    }

    @Override
    public List<Input> getInputs() {
        return inputs;
    }

    @Override
    public List<Output> getOutputs() {
        return outputs;
    }
}
