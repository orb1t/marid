/*
 * Copyright (c) 2015 Dmitry Ovchinnikov
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

package org.marid.ide.cli;

import org.marid.image.MaridIcon;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

/**
 * @author Dmitry Ovchinnikov.
 */
public class CommandLineBackgroundCache {

    private final Map<Long, BufferedImage> imageMap = new LinkedHashMap<>(32, 0.75f, true);

    public BufferedImage getImage(int width, int height) {
        final Long key = key(width, height);
        synchronized (imageMap) {
            final BufferedImage image = imageMap.computeIfAbsent(key, k -> {
                final BufferedImage img = new BufferedImage(width, height, TYPE_INT_ARGB);
                final Graphics2D g = img.createGraphics();
                try {
                    g.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_ATOP, 0.01f));
                    MaridIcon.draw(width, height, Color.GREEN, g);
                } finally {
                    g.dispose();
                }
                return img;
            });
            if (imageMap.size() >= 32) {
                int i = 0;
                for (final Iterator it = imageMap.entrySet().iterator(); it.hasNext(); ) {
                    it.next();
                    if (++i >= 32) {
                        it.remove();
                    }
                }
            }
            return image;
        }
    }

    private long key(int width, int height) {
        return ByteBuffer.allocate(8).putInt(0, width).putInt(4, height).getLong(0);
    }
}
