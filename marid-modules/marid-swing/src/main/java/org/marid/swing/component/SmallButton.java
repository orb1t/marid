/*
 * Copyright (c) 2015 Dmitry Ovchinnikov
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

package org.marid.swing.component;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import static java.awt.event.ActionEvent.ACTION_PERFORMED;

/**
 * @author Dmitry Ovchinnikov.
 */
public class SmallButton extends JLabel {

    private final Action action;
    private boolean hover;

    public SmallButton(@Nonnull Action action, boolean textEnabled) {
        this.action = action;
        if (action.getValue(Action.SMALL_ICON) != null) {
            setIcon((Icon) action.getValue(Action.SMALL_ICON));
        }
        if (textEnabled && action.getValue(Action.NAME) != null) {
            setText(action.getValue(Action.NAME).toString());
        }
        if (action.getValue(Action.SHORT_DESCRIPTION) != null) {
            setToolTipText(action.getValue(Action.SHORT_DESCRIPTION).toString());
        }
        enableEvents(MouseEvent.MOUSE_EVENT_MASK);
        setOpaque(false);
    }

    @Override
    protected void processMouseEvent(MouseEvent e) {
        super.processMouseEvent(e);
        switch (e.getID()) {
            case MouseEvent.MOUSE_ENTERED:
                hover = true;
                repaint();
                break;
            case MouseEvent.MOUSE_EXITED:
                hover = false;
                repaint();
                break;
            case MouseEvent.MOUSE_CLICKED:
                action.actionPerformed(new ActionEvent(e.getSource(), ACTION_PERFORMED, "do"));
                break;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (!hover) {
            ((Graphics2D) g).setComposite(AlphaComposite.Xor);
        }
        super.paintComponent(g);
    }
}
