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

package images

import javax.swing.ImageIcon
import java.awt.Dimension
import java.awt.GraphicsConfiguration
import java.awt.GraphicsEnvironment
import java.awt.Image
import java.awt.Toolkit
import java.awt.image.BufferedImage
import java.awt.image.ColorModel

class Images {

    static BufferedImage getEmptyImage(final Dimension size) {
        return getEmptyImage(size.width, size.height);
    }

    static BufferedImage getEmptyImage(int width, int height) {
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsConfiguration conf = env.getDefaultScreenDevice().getDefaultConfiguration();
        return conf.createCompatibleImage(width, height, ColorModel.TRANSLUCENT);
    }

    static ImageIcon getIcon(String path) {
        URL url = path == null ? null : Images.class.getResource(path);
        return url == null ? null : new ImageIcon(url);
    }

    static ImageIcon getIcon(String path, int width, int height) {
        URL url = path == null ? null : Images.class.getResource(path);
        if (url == null) {
            return null;
        } else {
            Image img = new ImageIcon(url).getImage();
            return new ImageIcon(img.getScaledInstance(width, height, Image.SCALE_SMOOTH));
        }
    }

    static ImageIcon getIcon(String path, Dimension size) {
        return getIcon(path, size.width, size.height);
    }

    static Image getImage(String path) {
        URL url = Images.class.getResource(path);
        return url == null ? null : Toolkit.getDefaultToolkit().getImage(url);
    }

    static Image getImage(String path, int width, int height) {
        URL url = Images.class.getResource(path);
        if (url == null) {
            return null;
        } else {
            return Toolkit.getDefaultToolkit().getImage(url);
        }
    }

    static Image getImage(String path, Dimension size) {
        return getImage(path, size.width, size.height);
    }
}
