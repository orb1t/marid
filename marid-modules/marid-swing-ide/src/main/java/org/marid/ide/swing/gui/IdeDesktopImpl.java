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

package org.marid.ide.swing.gui;

import org.marid.ide.base.IdeDesktop;
import org.marid.image.MaridIcon;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;

import static java.awt.SystemColor.desktop;

/**
 * @author Dmitry Ovchinnikov.
 */
@Component
public class IdeDesktopImpl extends JDesktopPane implements IdeDesktop {

    private final Color lc = new Color(77, 77, 77, 77);
    private final Color hc = new Color(177, 177, 177, 77);
    private final AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.02f);

    public IdeDesktopImpl() {
        setOpaque(true);
        setDoubleBuffered(true);
        setBackground(desktop);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        final Graphics2D g = (Graphics2D) graphics;
        final Rectangle clip = g.getClipBounds();
        g.setBackground(getBackground().darker());
        g.clearRect(clip.x, clip.y, clip.width, clip.height);
        final int cx = getWidth() / 2, cy = getHeight() / 2;
        final int minSize = Math.min(getWidth(), getHeight()), size = minSize < 128 ? 64 : minSize - 64;
        final Graphics2D cg = (Graphics2D) g.create(cx - size / 2, cy - size / 2, size, size);
        try {
            cg.setComposite(composite);
            MaridIcon.draw(size, Color.GREEN, cg);
        } finally {
            cg.dispose();
        }
        g.setPaint(new GradientPaint(cx, 0, hc, cx, getHeight(), lc, false));
        g.fillRect(clip.x, clip.y, clip.width, clip.height);
    }
}
