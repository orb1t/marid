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

package org.marid.bd.components;

import org.marid.bd.BlockComponent;
import org.marid.swing.geom.ShapeUtils;

import javax.swing.border.AbstractBorder;
import java.awt.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class DefaultBlockComponentBorder extends AbstractBorder {

    private static final Insets DEFAULT_INSETS = new Insets(0, 10, 0, 10);

    @Override
    public void paintBorder(Component c, Graphics graphics, int x, int y, int width, int height) {
        if (!(c instanceof BlockComponent)) {
            return;
        }
        final BlockComponent blockComponent = (BlockComponent) c;
        final Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.translate(x, y);
            g.setColor(SystemColor.controlDkShadow);
            for (final BlockComponent.Input input : blockComponent.getInputs()) {
                final Rectangle bounds = ShapeUtils.toParent(input.getButton(), c);
                final int ly = (int) bounds.getCenterY();
                g.drawLine(0, ly, DEFAULT_INSETS.left, ly);
            }
            for (final BlockComponent.Output output : blockComponent.getOutputs()) {
                final Rectangle bounds = ShapeUtils.toParent(output.getButton(), c);
                final int ly = (int) bounds.getCenterY();
                g.drawLine(width - DEFAULT_INSETS.right, ly, width, ly);
            }
        } finally {
            g.dispose();
        }
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return DEFAULT_INSETS;
    }
}
