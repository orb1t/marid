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

import org.marid.bd.Block;
import org.marid.bd.BlockComponent;
import org.marid.bd.NamedBlock;
import org.marid.bd.components.DefaultBlockComponentBorder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.util.Collections;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov
 */
public class ConstantBlockComponent extends JPanel implements BlockComponent {

    protected final ConstantBlock constantBlock;
    protected final ConstantBlockComponentOutput output = new ConstantBlockComponentOutput();
    protected final NamedBlock.ChangeNameListener changeNameListener = e -> output.setText(e.newValue);

    protected ConstantBlockComponent(ConstantBlock constantBlock) {
        super(new BorderLayout());
        this.constantBlock = constantBlock;
        add(output);
        setOpaque(false);
        setBorder(new DefaultBlockComponentBorder());
        enableEvents(HierarchyEvent.HIERARCHY_EVENT_MASK);
    }

    @Override
    protected void processHierarchyEvent(HierarchyEvent e) {
        super.processHierarchyEvent(e);
        switch (e.getID()) {
            case HierarchyEvent.SHOWING_CHANGED:
                if (isShowing()) {
                    constantBlock.addEventListener(NamedBlock.ChangeNameListener.class, changeNameListener);
                } else {
                    constantBlock.removeEventListener(NamedBlock.ChangeNameListener.class, changeNameListener);
                }
                break;
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

    protected class ConstantBlockComponentOutput extends JToggleButton implements Output {

        public ConstantBlockComponentOutput() {
            super("constantBlock");
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
