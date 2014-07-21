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

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.function.Supplier;

/**
 * @author Dmitry Ovchinnikov
 */
public class BlockLabel extends JComponent {

    protected final Supplier<String> textSupplier;
    protected final Supplier<Color> colorSupplier;

    public BlockLabel(Supplier<String> textSupplier, Supplier<Color> colorSupplier) {
        this.textSupplier = textSupplier;
        this.colorSupplier = colorSupplier;
        setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD));
    }

    @Override
    public Dimension getPreferredSize() {
        final String text = textSupplier.get();
        final Rectangle2D b = getFont().getStringBounds(text, getFontMetrics(getFont()).getFontRenderContext());
        final int w = (int) (b.getWidth() - b.getX());
        final int h = (int) (b.getHeight() - b.getY());
        return new Dimension(w + 10, h + 10);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        final String text = textSupplier.get();
        final Color color = colorSupplier.get();
        final Graphics2D g = (Graphics2D) graphics;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        final Dimension d = getPreferredSize();
        final Dimension size = getSize();
        final Rectangle2D b = getFont().getStringBounds(text, getFontMetrics(getFont()).getFontRenderContext());
        g.setColor(color);
        g.fillRoundRect((size.width - d.width) / 2, (size.height - d.height) / 2, d.width, d.height, 5, 5);
        g.setColor(Color.WHITE);
        final float x = (float) (size.width - b.getWidth() - b.getX()) / 2.0f;
        final float y = (float) (size.height - b.getHeight() - b.getY()) / 2.0f + 5;
        g.drawString(text, x, y);
    }
}
