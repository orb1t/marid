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

package org.marid.bd.schema;

import org.marid.bd.Block;
import org.marid.swing.actions.MaridAction;
import org.marid.swing.util.MessageSupport;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;

/**
 * @author Dmitry Ovchinnikov
 */
public class SchemaEditorPopupMenu extends JPopupMenu implements MessageSupport {

    private Block block;
    private Point dropPoint;
    private final MaridAction pasteAction = new MaridAction("Paste", "paste", this::dropBlock);

    public SchemaEditorPopupMenu() {
        add(pasteAction);
    }

    @Override
    public SchemaEditor getInvoker() {
        return (SchemaEditor) super.getInvoker();
    }

    @Override
    public void show(Component invoker, int x, int y) {
        block = clipboardBlock();
        dropPoint = new Point(x, y);
        pasteAction.setEnabled(block != null);
        super.show(invoker, x, y);
    }

    private void dropBlock(ActionEvent event) {
        getInvoker().dropBlock(block, dropPoint, TransferHandler.COPY);
    }

    private Block clipboardBlock() {
        final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        final Transferable transferable = clipboard.getContents(getInvoker());
        if (transferable == null) {
            return null;
        } else {
            final DataFlavor[] dataFlavors = transferable.getTransferDataFlavors();
            if (dataFlavors == null || dataFlavors.length == 0) {
                return null;
            } else {
                try {
                    final Object object = transferable.getTransferData(dataFlavors[0]);
                    return object instanceof Block ? (Block) object : null;
                } catch (Exception e) {
                    showMessage(WARNING_MESSAGE, "Unable to paste", e);
                }
                return null;
            }
        }
    }
}
