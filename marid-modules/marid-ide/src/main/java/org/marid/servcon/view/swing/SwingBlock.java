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

package org.marid.servcon.view.swing;

import images.Images;
import org.marid.servcon.model.Block;
import org.marid.swing.MaridAction;
import org.marid.swing.adapters.ComponentDragger;
import org.marid.swing.adapters.MouseMotionDelegate;

import javax.swing.*;
import java.awt.*;

import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov.
 */
public class SwingBlock extends JPanel implements SwingServconConstants {

    private final BlockEditor parent;
    private final Block block;

    public SwingBlock(BlockEditor parent, Block block) {
        super(new BorderLayout(5, 5));
        this.block = block;
        this.parent = parent;
        setBorder(BLOCK_BORDER);
        addMouseMotionListener(new ComponentDragger(this));
        final JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBorder(BorderFactory.createEtchedBorder());
        toolBar.addMouseMotionListener(new MouseMotionDelegate(this));
        toolBar.add(new JLabel(block.toString(), Images.getIcon(block.getMetaInfo().icon()), JLabel.LEFT));
        toolBar.add(Box.createGlue());
        toolBar.addSeparator();
        toolBar.add(new MaridAction("Information", "info", (a, e) -> {
        })).setFocusable(false);
        toolBar.addSeparator();
        toolBar.add(new MaridAction("Close", "removeWidget", (a, e) -> {
            final Rectangle bounds = getBounds();
            parent.remove(this);
            parent.repaint(bounds);
        })).setFocusable(false);
        add(toolBar, BorderLayout.NORTH);
        final JPanel leftPanel = new JPanel(new GridLayout(0, 1, 2, 2));
        final Block.Param[] params = block.getParameters();
        if (params.length > 0) {
            leftPanel.add(new Title("Parameters"));
            for (final Block.Param param : params) {
                leftPanel.add(new Param(param));
            }
        }
        final JPanel rightPanel = new JPanel(new GridLayout(0, 1, 2, 2));
        rightPanel.add(new Title("Outputs"));
        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);
    }

    private class Title extends JLabel {

        private Title(String title) {
            super(s(title));
            setBorder(TITLE_BORDER);
        }
    }

    private class Param extends JLabel {

        private final Block.Param param;

        private Param(Block.Param param) {
            super(param.toString(), Images.getIcon(param.getMetaInfo().icon()), SwingConstants.LEFT);
            this.param = param;
        }
    }
}
