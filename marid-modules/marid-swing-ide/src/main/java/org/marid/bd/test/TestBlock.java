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

package org.marid.bd.test;

import org.marid.bd.Block;
import org.marid.bd.BlockComponent;
import org.marid.bd.components.DefaultBlockComponentBorder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov
 */
public class TestBlock extends Block {
    @Override
    public BlockComponent createComponent() {
        return new TestBlockComponent();
    }

    @Override
    public Window createWindow() {
        return null;
    }

    @Override
    public List<Input<?>> getInputs() {
        return Collections.emptyList();
    }

    @Override
    public List<Output<?>> getOutputs() {
        return Collections.emptyList();
    }

    @Override
    public String getName() {
        return "test";
    }

    private class TestBlockComponent extends JPanel implements BlockComponent {

        public TestBlockComponent() {
            setBorder(new DefaultBlockComponentBorder());
            add(new JToggleButton(new AbstractAction("test1") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.printf("%s%n", e);
                }
            }));
            add(new JToggleButton("test2"));
            add(new JToggleButton("test3"));
            add(new JButton("xx"));
            final JPanel panel = new JPanel();
            panel.add(new JButton("y"));
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            add(panel);
        }

        @Override
        public Block getBlock() {
            return TestBlock.this;
        }

        @Override
        public List<Input> getInputs() {
            return Collections.emptyList();
        }

        @Override
        public List<Output> getOutputs() {
            return Collections.emptyList();
        }
    }
}
