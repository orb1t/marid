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

    private final AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.01f);

    public IdeDesktopImpl() {
        setOpaque(true);
        setDoubleBuffered(true);
        setBackground(desktop);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        final Graphics2D g = (Graphics2D) graphics.create();
        try {
            final Rectangle clip = g.getClipBounds();
            g.setBackground(getBackground().darker());
            g.clearRect(clip.x, clip.y, clip.width, clip.height);
            g.setComposite(composite);
            MaridIcon.draw(getWidth(), getHeight(), Color.GREEN, g);
        } finally {
            g.dispose();
        }
    }
}
