/*
 * Copyright (C) 2015 Dmitry Ovchinnikov
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

import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import java.util.function.Consumer;

/**
 * @author Dmitry Ovchinnikov
 */
public class InternalFrameAction implements InternalFrameListener {

    private final Consumer<InternalFrameEvent> eventConsumer;

    public InternalFrameAction(Consumer<InternalFrameEvent> eventConsumer) {
        this.eventConsumer = eventConsumer;
    }

    @Override
    public void internalFrameOpened(InternalFrameEvent e) {
        eventConsumer.accept(e);
    }

    @Override
    public void internalFrameClosing(InternalFrameEvent e) {
        eventConsumer.accept(e);
    }

    @Override
    public void internalFrameClosed(InternalFrameEvent e) {
        eventConsumer.accept(e);
    }

    @Override
    public void internalFrameIconified(InternalFrameEvent e) {
        eventConsumer.accept(e);
    }

    @Override
    public void internalFrameDeiconified(InternalFrameEvent e) {
        eventConsumer.accept(e);
    }

    @Override
    public void internalFrameActivated(InternalFrameEvent e) {
        eventConsumer.accept(e);
    }

    @Override
    public void internalFrameDeactivated(InternalFrameEvent e) {
        eventConsumer.accept(e);
    }
}
