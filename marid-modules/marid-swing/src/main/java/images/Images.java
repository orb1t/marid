/*
 * Copyright (C) 2013 Dmitry Ovchinnikov
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
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsConfiguration conf = env.getDefaultScreenDevice().getDefaultConfiguration();
        return conf.createCompatibleImage(width, height, ColorModel.TRANSLUCENT);
    }

    public static ImageIcon getIcon(String path) {
        URL url = path == null ? null : Images.class.getResource(path);
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
        URL url = path == null ? null : Images.class.getResource(path);
        if (url == null) {
            return null;
        } else {
            Toolkit tk = Toolkit.getDefaultToolkit();
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
        URL url = path == null ? null : Images.class.getResource(path);
        return url == null ? null : Toolkit.getDefaultToolkit().getImage(url);
    }
}
