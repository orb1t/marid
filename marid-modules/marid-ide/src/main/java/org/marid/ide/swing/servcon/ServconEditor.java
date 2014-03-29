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

package org.marid.ide.swing.servcon;

import org.marid.swing.MaridAction;
import org.marid.swing.adapters.ComponentDragger;
import org.marid.swing.adapters.MouseMotionDelegate;
import org.marid.swing.dnd.DndTarget;
import org.marid.swing.dnd.MaridTransferHandler;
import org.marid.swing.layout.AbsoluteLayout;

import javax.swing.*;
import java.awt.*;

import static java.awt.BorderLayout.NORTH;
import static java.awt.BorderLayout.WEST;
import static javax.swing.BorderFactory.*;

/**
 * @author Dmitry Ovchinnikov.
 */
public class ServconEditor extends JPanel implements DndTarget<ServconService> {

    public ServconEditor() {
        super(new AbsoluteLayout());
        setBackground(SystemColor.controlLtHighlight);
        setTransferHandler(new MaridTransferHandler());
    }

    @Override
    public boolean dropDndObject(ServconService object, TransferHandler.TransferSupport support) {
        add(new SsBlock(object, support));
        return true;
    }

    private class SsBlock extends JPanel {

        private final ServconService servconService;

        private SsBlock(ServconService servconService, TransferHandler.TransferSupport support) {
            super(new BorderLayout());
            this.servconService = servconService;
            setBorder(createCompoundBorder(createRaisedSoftBevelBorder(), createEmptyBorder(3, 3, 3, 3)));
            addMouseMotionListener(new ComponentDragger(this));
            final JPanel titlePanel = new JPanel(new BorderLayout());
            titlePanel.setBorder(createCompoundBorder(createEtchedBorder(), createEmptyBorder(2, 2, 2, 2)));
            titlePanel.add(new JLabel(servconService.getVisualRepresentation()), WEST);
            add(titlePanel, NORTH);
            final JToolBar toolBar = new JToolBar();
            toolBar.setFloatable(false);
            toolBar.setBorderPainted(false);
            toolBar.addMouseMotionListener(new MouseMotionDelegate(this));
            toolBar.addSeparator();
            toolBar.add(new JLabel(servconService.getObject().getSimpleName()));
            toolBar.add(Box.createGlue());
            toolBar.addSeparator();
            toolBar.add(new MaridAction("Information", "info", (a, e) -> {

            })).setFocusable(false);
            toolBar.addSeparator();
            toolBar.add(new MaridAction("Preferences", "settings", (a, e) -> {

            })).setFocusable(false);
            toolBar.addSeparator();
            toolBar.add(new MaridAction("Close", "removeWidget", (a, e) -> {
                final Rectangle bounds = getBounds();
                ServconEditor.this.remove(this);
                ServconEditor.this.repaint(bounds);
            })).setFocusable(false);
            titlePanel.add(toolBar);
            final JLabel caption = new JLabel(servconService.toString(), SwingConstants.CENTER);
            caption.setBorder(createEmptyBorder(5, 5, 5, 5));
            add(caption);
            setLocation(support.getDropLocation().getDropPoint());
        }
    }
}
