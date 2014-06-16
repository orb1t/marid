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

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.function.Consumer;

/**
 * @author Dmitry Ovchinnikov
 */
public class WindowAction implements WindowListener {

    private final Consumer<WindowEvent> consumer;

    public WindowAction(Consumer<WindowEvent> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void windowOpened(WindowEvent e) {
        consumer.accept(e);
    }

    @Override
    public void windowClosing(WindowEvent e) {
        consumer.accept(e);
    }

    @Override
    public void windowClosed(WindowEvent e) {
        consumer.accept(e);
    }

    @Override
    public void windowIconified(WindowEvent e) {
        consumer.accept(e);
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
        consumer.accept(e);
    }

    @Override
    public void windowActivated(WindowEvent e) {
        consumer.accept(e);
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
        consumer.accept(e);
    }
}
