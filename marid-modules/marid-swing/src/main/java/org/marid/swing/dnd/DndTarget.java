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

import org.marid.logging.LogSupport;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static javax.swing.TransferHandler.TransferSupport;

/**
 * @author Dmitry Ovchinnikov.
 */
public interface DndTarget<T extends DndObject> extends DndConstants, LogSupport {

    @SuppressWarnings("unchecked")
    default Class<T> getTargetDndType() {
        for (final Type type : getClass().getGenericInterfaces()) {
            if (type instanceof ParameterizedType) {
                final ParameterizedType pt = (ParameterizedType) type;
                if (pt.getRawType() == DndTarget.class) {
                    return (Class<T>) pt.getActualTypeArguments()[0];
                }
            }
        }
        return null;
    }

    default DataFlavor[] getTargetDataFlavors() {
        final Class<T> dndType = getTargetDndType();
        return dndType == null ? new DataFlavor[0] : new DataFlavor[] {new DataFlavor(dndType, null)};
    }

    default int getTargetDndActionsSupported() {
        return DND_NONE;
    }

    @SuppressWarnings("unchecked")
    default T getImported(Transferable transferable) {
        for (final DataFlavor dataFlavor : transferable.getTransferDataFlavors()) {
            if (dataFlavor.getRepresentationClass() == getTargetDndType()) {
                try {
                    return (T) transferable.getTransferData(dataFlavor);
                } catch (Exception x) {
                    warning("Unable to get transferable from {0}", x, transferable);
                }
            }
        }
        return null;
    }

    default boolean canImport(TransferSupport support) {
        return true;
    }

    default boolean dropDndObject(T object, TransferSupport support) {
        return false;
    }
}
