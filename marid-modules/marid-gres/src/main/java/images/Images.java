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

package images;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.net.URL;

/**
 * @author Dmitry Ovchinnikov
 */
public class Images {

    public static BufferedImage getEmptyImage(Dimension dimension) {
        return getEmptyImage(dimension.width, dimension.height);
    }

    public static BufferedImage getEmptyImage(int size) {
        return getEmptyImage(size, size);
    }

    public static BufferedImage getEmptyImage(int width, int height) {
        final GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsConfiguration conf = env.getDefaultScreenDevice().getDefaultConfiguration();
        return conf.createCompatibleImage(width, height, ColorModel.TRANSLUCENT);
    }

    public static ImageIcon getIcon(String path) {
        final URL url = path == null ? null : Images.class.getResource(path);
        return url == null ? null : new ImageIcon(url);
    }

    public static ImageIcon getIcon(String path, int w, int h) {
        final URL url = path == null ? null : Images.class.getResource(path);
        if (url == null) {
            return null;
        } else {
            final ImageIcon imageIcon = new ImageIcon(url);
            if (imageIcon.getIconHeight() != w || imageIcon.getIconHeight() != h) {
                return new ImageIcon(imageIcon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
            } else {
                return imageIcon;
            }
        }
    }

    public static ImageIcon getIcon(String path, Dimension dimension) {
        return getIcon(path, dimension.width, dimension.height);
    }

    public static ImageIcon getIcon(String path, int size) {
        return getIcon(path, size, size);
    }

    public static Image getImage(String path, int w, int h) {
        final URL url = path == null ? null : Images.class.getResource(path);
        if (url == null) {
            return null;
        } else {
            final Toolkit tk = Toolkit.getDefaultToolkit();
            return tk.getImage(url).getScaledInstance(w, h, Image.SCALE_SMOOTH);
        }
    }

    public static Image getImage(String path, Dimension dimension) {
        return getImage(path, dimension.width, dimension.height);
    }

    public static Image getImage(String path, int size) {
        return getImage(path, size, size);
    }

    public static Image getImage(String path) {
        final URL url = path == null ? null : Images.class.getResource(path);
        return url == null ? null : Toolkit.getDefaultToolkit().getImage(url);
    }

    public static BufferedImage getImageFromText(String text, int width, int height, Color back, Color fore) {
        final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = image.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setColor(back);
            g.fillRoundRect(0, 0, width, height, width / 3, height / 3);
            g.translate(3, 3);
            width -= 6;
            height -= 6;
            g.setColor(fore);
            g.setFont(new Font(Font.DIALOG, Font.BOLD, 12));
            final Rectangle2D bounds = g.getFont().getStringBounds(text, g.getFontRenderContext());
            final float yo = - (float) bounds.getY() + height / 2.0f - (float) bounds.getHeight() / 2.0f;
            final double w = bounds.getWidth();
            final double h = bounds.getHeight() - (float) bounds.getY();
            final double sx = width / w;
            final double sy = height / h;
            g.scale(sx, sy);
            g.drawString(text, 0.0f, yo / (float) sy);
        } finally {
            g.dispose();
        }
        return image;
    }

    public static ImageIcon getIconFromText(String text, int width, int height, Color back, Color fore) {
        return new ImageIcon(getImageFromText(text, width, height, back, fore));
    }
}
