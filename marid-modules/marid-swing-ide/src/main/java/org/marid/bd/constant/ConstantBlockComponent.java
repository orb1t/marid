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
import org.marid.swing.actions.AncestorAction;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import java.util.Collections;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov
 */
public class ConstantBlockComponent extends JToggleButton implements BlockComponent, BlockComponent.Output {

    protected final ConstantBlock constantBlock;

    protected ConstantBlockComponent(ConstantBlock constantBlock) {
        super(constantBlock.getName());
        this.constantBlock = constantBlock;
        final NamedBlock.ChangeNameListener changeNameListener = e -> setText(e.newValue);
        addAncestorListener(new AncestorAction(ev -> {
            switch (ev.getID()) {
                case AncestorEvent.ANCESTOR_ADDED:
                    constantBlock.addEventListener(NamedBlock.ChangeNameListener.class, changeNameListener);
                    break;
                case AncestorEvent.ANCESTOR_REMOVED:
                    constantBlock.removeEventListener(NamedBlock.ChangeNameListener.class, changeNameListener);
                    break;
            }
        }));

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
        return Collections.singletonList(this);
    }

    @Override
    public Block.Output<?> getOutput() {
        return constantBlock.output;
    }

    @Override
    public BlockComponent getBlockComponent() {
        return this;
    }
}
