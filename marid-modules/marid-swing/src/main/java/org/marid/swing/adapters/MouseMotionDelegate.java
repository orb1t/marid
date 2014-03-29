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

package org.marid.swing.adapters;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

/**
 * @author Dmitry Ovchinnikov.
 */
public class MouseMotionDelegate implements MouseMotionListener {

    private final Component delegate;

    public MouseMotionDelegate(Component delegate) {
        this.delegate = delegate;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        final MouseEvent mouseEvent = SwingUtilities.convertMouseEvent(e.getComponent(), e, delegate);
        for (final MouseMotionListener mouseMotionListener : delegate.getMouseMotionListeners()) {
            mouseMotionListener.mouseDragged(mouseEvent);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        final MouseEvent mouseEvent = SwingUtilities.convertMouseEvent(e.getComponent(), e, delegate);
        for (final MouseMotionListener mouseMotionListener : delegate.getMouseMotionListeners()) {
            mouseMotionListener.mouseMoved(mouseEvent);
        }
    }
}
