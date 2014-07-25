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
import org.marid.itf.Named;
import org.marid.l10n.L10n;
import org.marid.swing.dnd.DndSource;
import org.marid.swing.dnd.MaridTransferHandler;

import javax.swing.*;
import javax.swing.event.MenuDragMouseEvent;
import javax.swing.event.MenuDragMouseListener;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.util.*;

import static org.marid.bd.BlockProvider.visit;

/**
 * @author Dmitry Ovchinnikov
 */
public class BlockMenuSupport {

    private static final Comparator<Block> BLOCK_COMPARATOR = Comparator.comparing(Named::getName);

    public static void fillMenu(JMenu menu) {
        final Map<String, Set<Block>> blockMap = new TreeMap<>();
        visit(p -> p.visit((g, b) -> blockMap.computeIfAbsent(g, v -> new TreeSet<>(BLOCK_COMPARATOR)).add(b)));
        blockMap.forEach((group, blocks) -> {
            final JMenu groupMenu = new JMenu(L10n.s(group));
            menu.add(groupMenu);
            blocks.forEach(block -> groupMenu.add(new BlockMenuItem(block)));
        });
    }

    protected static class BlockMenuItem extends JMenuItem implements DndSource<Block>, MenuDragMouseListener {

        protected final Block block;

        public BlockMenuItem(Block block) {
            super(block.getName(), block.getVisualRepresentation(22, 22));
            this.block = block;
            setTransferHandler(new MaridTransferHandler());
            addMenuDragMouseListener(this);
            addActionListener(e -> {
                final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                getTransferHandler().exportToClipboard(this, clipboard, DND_COPY);
            });
        }

        @Override
        public int getDndActions() {
            return DND_COPY;
        }

        @Override
        public Block getDndObject() {
            return block;
        }

        @Override
        public void menuDragMouseEntered(MenuDragMouseEvent e) {

        }

        @Override
        public void menuDragMouseExited(MenuDragMouseEvent e) {

        }

        @Override
        public void menuDragMouseDragged(MenuDragMouseEvent e) {
            getTransferHandler().exportAsDrag(this, e, DND_COPY);
        }

        @Override
        public void menuDragMouseReleased(MenuDragMouseEvent e) {

        }
    }
}
