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

package org.marid.bd.constant;

import org.marid.bd.components.DefaultBlockComponent;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov
 */
public class ConstantBlockComponent extends DefaultBlockComponent<ConstantBlock> implements ConstantBlockListener {

    protected final ConstantBlockComponentOutput output;
    protected final JLabel nameLabel;

    protected ConstantBlockComponent(ConstantBlock constantBlock) {
        super(new BorderLayout(), constantBlock);
        add(output = new ConstantBlockComponentOutput());
        add(nameLabel = new JLabel(block.getName()), BorderLayout.NORTH);
        nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        output.setToolTipText(block.getValue());
    }

    @Override
    public List<Input> getInputs() {
        return Collections.emptyList();
    }

    @Override
    public List<Output> getOutputs() {
        return Collections.singletonList(output);
    }

    @Override
    public void changedValue(String oldValue, String newValue) {
        output.setToolTipText(newValue);
    }

    @Override
    public void nameChanged(String oldName, String newName) {
        nameLabel.setText(newName);
    }

    protected class ConstantBlockComponentOutput extends DefaultOutput {

        public ConstantBlockComponentOutput() {
            super(block.output);
        }
    }
}
