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
import org.marid.bd.BlockComponent;
import org.marid.swing.dnd.DndTarget;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.function.Consumer;

/**
 * @author Dmitry Ovchinnikov
 */
public class SchemaEditor extends JComponent implements DndTarget<Block> {

    protected final AffineTransform transform = new AffineTransform();

    public void visitBlockComponents(Consumer<BlockComponent> componentConsumer) {
        for (int i = 0; i < getComponentCount(); i++) {
            final Component c = getComponent(i);
            if (c instanceof BlockComponent) {
                componentConsumer.accept((BlockComponent) c);
            }
        }
    }

    @Override
    public boolean dropDndObject(Block object, TransferHandler.TransferSupport support) {
        try {
            final Point dropPoint = new Point();
            transform.inverseTransform(support.getDropLocation().getDropPoint(), dropPoint);
            final BlockComponent blockComponent = object.createComponent();
            blockComponent.setBounds(new Rectangle(dropPoint, blockComponent.getPreferredSize()));
            add(blockComponent.getComponent());
            blockComponent.setVisible(false);
            repaint();
            return true;
        } catch (Exception x) {
            warning("Unable to transform coordinates", x);
            return false;
        }
    }
}
