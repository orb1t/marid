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

package org.marid.swing.menu;

import org.marid.swing.dnd.DndSource;
import org.marid.swing.dnd.MaridTransferHandler;

import javax.swing.*;
import javax.swing.event.MenuDragMouseEvent;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Dmitry Ovchinnikov
 */
public class DraggableMenuItem<T extends Serializable> extends JMenuItem implements DndSource<T> {

    private final Class<? extends T> type;
    private final Supplier<T> objectSupplier;

    private int actions = DND_COPY;

    public DraggableMenuItem(Class<? extends T> type, ImageIcon icon, String label, Supplier<T> objectSupplier) {
        super(label, icon);
        this.type = type;
        this.objectSupplier = objectSupplier;
        setTransferHandler(new MaridTransferHandler());
        addActionListener(e -> {
            final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            getTransferHandler().exportToClipboard(this, clipboard, DND_COPY);
        });
    }

    @Override
    public void processMenuDragMouseEvent(MenuDragMouseEvent e) {
        super.processMenuDragMouseEvent(e);
        switch (e.getID()) {
            case MenuDragMouseEvent.MOUSE_DRAGGED:
                getTransferHandler().exportAsDrag(this, e, DND_COPY);
                break;
        }
    }

    public DraggableMenuItem<T> actions(int actions) {
        this.actions |= actions;
        return this;
    }

    public DraggableMenuItem<T> doWith(Consumer<DraggableMenuItem<T>> consumer) {
        consumer.accept(this);
        return this;
    }

    @Override
    public int getDndActions() {
        return actions;
    }

    @Override
    public T getDndObject() {
        return objectSupplier.get();
    }

    @Override
    public DataFlavor[] getSourceDataFlavors() {
        return new DataFlavor[]{new DataFlavor(type, null)};
    }
}
