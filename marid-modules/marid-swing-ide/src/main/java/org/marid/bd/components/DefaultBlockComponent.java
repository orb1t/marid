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

import javax.swing.*;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.font.TextAttribute;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Map;

import static java.awt.font.TextAttribute.*;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class DefaultBlockComponent<B extends Block> extends JPanel implements BlockComponent {

    protected final B block;

    public DefaultBlockComponent(LayoutManager layoutManager, B block) {
        super(layoutManager);
        this.block = block;
        enableEvents(HierarchyEvent.HIERARCHY_EVENT_MASK);
        setOpaque(false);
    }

    @Override
    protected void processHierarchyEvent(HierarchyEvent e) {
        super.processHierarchyEvent(e);
        if (this instanceof EventListener && e.getID() == HierarchyEvent.HIERARCHY_CHANGED) {
            if (e.getChangedParent() != null) {
                block.addEventListener(this, (EventListener) this);
            } else {
                block.removeEventListeners(this);
            }
        }
    }

    @Override
    public B getBlock() {
        return block;
    }

    protected class DefaultInput extends ArrowButton implements Input {

        protected final Block.Input input;
        protected final JLabel label;

        public DefaultInput(Block.Input input) {
            super(input.getName(), SwingConstants.WEST);
            label = new JLabel(getName());
            final Font font = UIManager.getFont("Label.font");
            final Map<TextAttribute, Object> map = new HashMap<>();
            if (input.getInputType().isArray()) {
                map.put(UNDERLINE, UNDERLINE_LOW_DASHED);
            }
            if (input.isRequired()) {
                map.put(WEIGHT, WEIGHT_BOLD);
            }
            label.setFont(map.isEmpty() ? font : font.deriveFont(map));
            addActionListener(e -> getBlockComponent().getSchemaEditor().visitBlockComponents(bc -> {
                if (bc != getBlockComponent()) {
                    bc.getOutputs().forEach(o -> {
                        if (o.getButton().isSelected()) {
                            o.getButton().setSelected(false);
                            getBlockComponent().getSchemaEditor().addLink(o, DefaultInput.this);
                        }
                    });
                }
                getButton().setSelected(false);
                getBlockComponent().getSchemaEditor().repaint();
            }));
            this.input = input;
        }

        @Override
        public Block.Input getInput() {
            return input;
        }

        @Override
        public DefaultBlockComponent getBlockComponent() {
            return DefaultBlockComponent.this;
        }

        public JLabel getAssociatedLabel() {
            return label;
        }

        @Override
        public BaselineResizeBehavior getBaselineResizeBehavior() {
            return label.getBaselineResizeBehavior();
        }

        @Override
        public int getBaseline(int width, int height) {
            return label.getBaseline(width, height);
        }
    }

    protected class DefaultOutput extends ArrowButton implements Output {

        protected final Block.Output output;
        protected final JLabel label;

        public DefaultOutput(Block.Output output) {
            super(output.getName(), SwingConstants.EAST);
            this.label = new JLabel(getName());
            this.output = output;
        }

        @Override
        public Block.Output getOutput() {
            return output;
        }

        @Override
        public DefaultBlockComponent getBlockComponent() {
            return DefaultBlockComponent.this;
        }

        public JLabel getAssociatedLabel() {
            return label;
        }

        @Override
        public BaselineResizeBehavior getBaselineResizeBehavior() {
            return label.getBaselineResizeBehavior();
        }

        @Override
        public int getBaseline(int width, int height) {
            return label.getBaseline(width, height);
        }
    }
}
