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

package org.marid.servcon.view;

import org.marid.servcon.model.Block;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov.
 */
public class VBlock extends VPaintable {

    protected VBlock(BlockEditor blockEditor, Block block, Point location) {
        super(blockEditor, block, location);
    }

    @Override
    public Dimension getSize() {
        //System.out.println(blockEditor.getFontMetrics().getStringBounds("hello", blockEditor.getGraphics()));
        return new Dimension(50, 50);
    }

    @Override
    protected List<? extends Region> getRegions() {
        return Collections.emptyList();
    }

    @Override
    protected void paint(Graphics2D g) {
        final Dimension size = getSize();
        g.setColor(SystemColor.control);
        g.fillRect(0, 0, size.width, size.height);
        g.setColor(SystemColor.controlShadow);
        g.draw3DRect(0, 0, size.width, size.height, true);
        final ImageIcon icon = block.getVisualRepresentation();
        if (icon != null) {
            icon.paintIcon(blockEditor, g, 2, 2);
        }
        super.paint(g);
    }
}
