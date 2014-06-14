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
import org.marid.bd.constant.ConstantBlock;
import org.marid.bd.test.TestBlock;
import org.marid.pref.PrefSupport;
import org.marid.swing.dnd.DndSource;
import org.marid.swing.dnd.MaridTransferHandler;

import javax.swing.*;
import java.awt.*;
import java.util.prefs.Preferences;

/**
 * @author Dmitry Ovchinnikov
 */
public class BlockListWindow extends JDialog implements PrefSupport {

    protected final BlockList blockList = new BlockList();
    protected final Preferences preferences;

    public BlockListWindow(SchemaFrame schemaFrame) {
        super(schemaFrame, "Block list", false);
        preferences = schemaFrame.preferences();
        add(new JScrollPane(blockList));
        setPreferredSize(new Dimension(300, 500));
        pack();
        setLocation(getPref("blockListLocation", new Point(0, 0)));
        setSize(getPref("blockListSize", getPreferredSize()));
    }

    @Override
    public void dispose() {
        try {
            putPref("blockListLocation", getLocation());
            putPref("blockListSize", getSize());
        } finally {
            super.dispose();
        }
    }

    @Override
    public Preferences preferences() {
        return preferences;
    }

    protected class BlockList extends JList<Block> implements DndSource<Block> {

        public BlockList() {
            super(new Block[] {new ConstantBlock(), new TestBlock()});
            setTransferHandler(new MaridTransferHandler());
            setDragEnabled(true);
        }

        @Override
        public int getDndActions() {
            return DND_COPY;
        }

        @Override
        public Block getDndObject() {
            return getSelectedValue();
        }
    }
}
