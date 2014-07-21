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

package org.marid.swing.actions;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.function.Consumer;

/**
 * @author Dmitry Ovchinnikov
 */
public class MouseAction implements MouseListener {

    private final Consumer<MouseEvent> consumer;

    public MouseAction(Consumer<MouseEvent> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        consumer.accept(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        consumer.accept(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        consumer.accept(e);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        consumer.accept(e);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        consumer.accept(e);
    }
}
