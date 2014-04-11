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

package org.marid.swing.dnd;

import images.Images;
import org.marid.logging.LogSupport;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import static java.util.Arrays.stream;

/**
 * @author Dmitry Ovchinnikov.
 */
public class MaridTransferHandler extends TransferHandler implements LogSupport {

    public MaridTransferHandler() {
        setDragImage(Images.getImage("add.png", 16));
        setDragImageOffset(new Point(0, 0));
    }

    @Override
    public int getSourceActions(JComponent c) {
        return c instanceof DndSource ? ((DndSource) c).getDndActions() : NONE;
    }

    @Override
    public Icon getVisualRepresentation(Transferable t) {
        for (final DataFlavor dataFlavor : t.getTransferDataFlavors()) {
            final Class<?> rc = dataFlavor.getRepresentationClass();
            if (rc != null && DndObject.class.isAssignableFrom(rc)) {
                try {
                    final DndObject dndObject = (DndObject) t.getTransferData(dataFlavor);
                    return dndObject.getVisualRepresentation();
                } catch (UnsupportedFlavorException | IOException x) {
                    warning("Unable to get visual representation for {0}", x, t);
                }
            }
        }
        return null;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        if (c instanceof DndSource) {
            final DndSource dndSource = (DndSource) c;
            final DataFlavor[] dataFlavors = dndSource.getSourceDataFlavors();
            final DndObject data = dndSource.getDndObject();
            return new Transferable() {
                @Override
                public DataFlavor[] getTransferDataFlavors() {
                    return dataFlavors;
                }

                @Override
                public boolean isDataFlavorSupported(DataFlavor flavor) {
                    for (final DataFlavor df : dataFlavors) {
                        if (df.match(flavor)) {
                            return true;
                        }
                    }
                    return false;
                }

                @SuppressWarnings("unchecked")
                @Override
                public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
                    if (!isDataFlavorSupported(flavor)) {
                        throw new UnsupportedFlavorException(flavor);
                    }
                    return dndSource.encodeDndSource(flavor, data);
                }
            };
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {
        if (source instanceof DndSource) {
            for (final DataFlavor dataFlavor : data.getTransferDataFlavors()) {
                final Class<?> rc = dataFlavor.getRepresentationClass();
                if (rc != null && DndObject.class.isAssignableFrom(rc)) {
                    try {
                        final DndObject dndObject = (DndObject) data.getTransferData(dataFlavor);
                        ((DndSource) source).fireDndObjectExportDone(dndObject, action);
                    } catch (UnsupportedFlavorException | IOException x) {
                        warning("Unable to get transfer data for {0}", x, data);
                    }
                }
            }
        }
    }

    @Override
    public boolean canImport(TransferSupport support) {
        if (support.getComponent() instanceof DndTarget) {
            final DndTarget dndTarget = (DndTarget) support.getComponent();
            final DataFlavor[] supported = dndTarget.getTargetDataFlavors();
            return stream(support.getDataFlavors())
                    .anyMatch(df -> stream(supported).anyMatch(sdf -> sdf.match(df))) && dndTarget.canImport(support);
        } else {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean importData(TransferSupport support) {
        if (support.getComponent() instanceof DndTarget) {
            final DndTarget dndTarget = (DndTarget) support.getComponent();
            final DndObject dndObject = dndTarget.getImported(support.getTransferable());
            return dndObject != null && dndTarget.dropDndObject(dndObject, support);
        } else {
            return false;
        }
    }
}
