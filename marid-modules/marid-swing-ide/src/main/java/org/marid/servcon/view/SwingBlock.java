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

package org.marid.servcon.view;

import org.marid.servcon.model.Block;
import org.marid.swing.MaridAction;

import javax.swing.*;

/**
 * @author Dmitry Ovchinnikov.
 */
public class SwingBlock extends JPanel {

    protected final BlockEditor blockEditor;
    protected final Block block;

    public SwingBlock(BlockEditor blockEditor, Block block) {
        this.blockEditor = blockEditor;
        this.block = block;
        setOpaque(true);
        setBorder(BorderFactory.createRaisedSoftBevelBorder());
        final GroupLayout g = new GroupLayout(this);
        g.setAutoCreateGaps(true);
        g.setAutoCreateContainerGaps(true);
        final JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBorder(BorderFactory.createEtchedBorder());
        toolBar.setBorderPainted(true);
        toolBar.add(new JLabel(block.toString(), block.getVisualRepresentation(), SwingConstants.LEFT));
        toolBar.add(Box.createGlue());
        toolBar.addSeparator();
        toolBar.add(new MaridAction("Remove block", "removeWidget", e -> {
            blockEditor.remove(this);
        }));
        g.setVerticalGroup(g.createSequentialGroup()
                .addComponent(toolBar));
        g.setHorizontalGroup(g.createParallelGroup()
                .addComponent(toolBar));
        setLayout(g);
        validate();
    }
}
