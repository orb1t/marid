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

package org.marid.ide.bde;

import org.marid.bde.model.Block;
import org.marid.bde.model.ClassBlock;
import org.marid.service.MaridServices;
import org.marid.swing.dnd.DndSource;
import org.marid.swing.dnd.MaridTransferHandler;
import org.marid.swing.model.StdListCellRenderer;

import javax.swing.*;

/**
 * @author Dmitry Ovchinnikov.
 */
public class ServconServices extends JList<Block> implements DndSource<Block> {

    public ServconServices() {
        super(MaridServices.serviceClasses().stream().map(ClassBlock::new).sorted().toArray(ClassBlock[]::new));
        setCellRenderer(new ServiceCellRenderer());
        setTransferHandler(new MaridTransferHandler());
        setDragEnabled(true);
    }

    @Override
    public Block getDndObject() {
        return getSelectedValue();
    }

    @Override
    public int getDndActions() {
        return DND_COPY;
    }

    private static class ServiceCellRenderer extends StdListCellRenderer<Block> {
        @Override
        public JLabel getRenderer(JList<Block> lst, Block val, int idx, boolean sel, boolean focus) {
            final JLabel r =  super.getRenderer(lst, val, idx, sel, focus);
            r.setIcon(val.getVisualRepresentation(32, 32));
            return r;
        }
    }
}
