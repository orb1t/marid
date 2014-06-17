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

import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.marid.bd.Block;
import org.marid.bd.BlockComponent;
import org.marid.bd.components.DefaultBlockComponentBorder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.util.Collections;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov
 */
public class ConstantBlockComponent extends JPanel implements BlockComponent, ConstantBlock.ConstantBlockListener {

    protected final ConstantBlock constantBlock;
    protected final ConstantBlockComponentOutput output;
    protected final JLabel valueLabel;

    protected ConstantBlockComponent(ConstantBlock constantBlock) {
        super(new BorderLayout());
        this.constantBlock = constantBlock;
        add(output = new ConstantBlockComponentOutput());
        add(valueLabel = new JLabel(String.valueOf(constantBlock.getValue().getValue())), BorderLayout.NORTH);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        setOpaque(false);
        setBorder(new DefaultBlockComponentBorder());
        enableEvents(HierarchyEvent.HIERARCHY_EVENT_MASK);
    }

    @Override
    protected void processHierarchyEvent(HierarchyEvent e) {
        super.processHierarchyEvent(e);
        if (e.getID() == HierarchyEvent.HIERARCHY_CHANGED) {
            if (e.getChangedParent() != null) {
                constantBlock.addEventListener(this, this);
            } else {
                constantBlock.removeEventListeners(this);
            }
        }
    }

    @Override
    public ConstantBlock getBlock() {
        return constantBlock;
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
    public void changedValue(ConstantExpression oldValue, ConstantExpression newValue) {
        valueLabel.setText(String.valueOf(newValue.getValue()));
    }

    @Override
    public void nameChanged(String oldName, String newName) {
        output.setText(newName);
    }

    protected class ConstantBlockComponentOutput extends JToggleButton implements Output {

        public ConstantBlockComponentOutput() {
            super(getBlock().getName());
        }

        @Override
        public Block.Output<?> getOutput() {
            return constantBlock.output;
        }

        @Override
        public BlockComponent getBlockComponent() {
            return ConstantBlockComponent.this;
        }
    }
}
