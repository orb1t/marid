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
import java.awt.image.ColorModel;
import java.net.URL;

/**
 * Images collection.
 * 
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public class Images {
    /**
     * Empty 16x16 image.
     */
    public static final Image IM16x16 = getEmptyImage(16, 16);
    
    /**
     * Empty 24x24 image.
     */
    public static final Image IM24x24 = getEmptyImage(24, 24);
    
    /**
     * Empty 32x32 image.
     */
    public static final Image IM32x32 = getEmptyImage(32, 32);
    
    /**
     * Empty 48x48 image.
     */
    public static final Image IM48x48 = getEmptyImage(48, 48);
    
    /**
     * Empty 16x16 icon.
     */
    public static final ImageIcon IC16x16 = new ImageIcon(IM16x16);
    
    /**
     * Empty 24x24 icon.
     */
    public static final ImageIcon IC24x24 = new ImageIcon(IM24x24);
    
    /**
     * Empty 32x32 icon.
     */
    public static final ImageIcon IC32x32 = new ImageIcon(IM32x32);
    
    /**
     * Empty 48x48 icon.
     */
    public static final ImageIcon IC48x48 = new ImageIcon(IM48x48);
    
    /**
     * Creates an empty image.
     * @param size Image size.
     * @return Empty image.
     */
    public static Image getEmptyImage(final Dimension size) {
        return getEmptyImage(size.width, size.height);
    }
    
    /**
     * Creates an empty image.
     * @param width Image width.
     * @param height Image height.
     * @return Empty image.
     */
    public static Image getEmptyImage(int width, int height) {
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsConfiguration conf = env.getDefaultScreenDevice().getDefaultConfiguration();
        return conf.createCompatibleImage(width, height, ColorModel.TRANSLUCENT);
    }
        
    /**
     * Creates an icon.
     * @param path Icon path.
     * @return Icon object.
     */
    public static ImageIcon getIcon(String path) {
        URL url = path == null ? null : Images.class.getResource(path);
        return url == null ? null : new ImageIcon(url);
    }
    
    /**
     * Get an icon.
     * @param path Icon path.
     * @param width Width.
     * @param height Height.
     * @return Icon object.
     */
    public static ImageIcon getIcon(String path, int width, int height) {
        URL url = path == null ? null : Images.class.getResource(path);
        if (url == null) {
            if (width == height) {
                switch (width) {
                    case 16:    return IC16x16;
                    case 24:    return IC24x24;
                    case 32:    return IC32x32;
                    case 48:    return IC48x48;
                }    
            }
            return new ImageIcon(getEmptyImage(width, height));
        } else {
            Image img = new ImageIcon(url).getImage();
            return new ImageIcon(img.getScaledInstance(
                    width, height, Image.SCALE_SMOOTH));
        }
    }
    
    /**
     * Get an icon.
     * @param path Icon path.
     * @param size Icon size.
     * @return Icon object.
     */
    public static ImageIcon getIcon(String path, Dimension size) {
        return getIcon(path, size.width, size.height);
    }
    
    /**
     * Get an image.
     * @param path Image path.
     * @return Image object.
     */
    public static Image getImage(String path) {
        URL url = path == null ? null : Images.class.getResource(path);
        return url == null ? null : Toolkit.getDefaultToolkit().getImage(url);
    }
    
    /**
     * Get an image.
     * @param path Image path.
     * @param width Image width.
     * @param height Image height.
     * @return Image object.
     */
    public static Image getImage(String path, int width, int height) {
        URL url = path == null ? null : Images.class.getResource(path);
        if (url == null) {
            if (width == height) {
                switch (width) {
                    case 16:    return IM16x16;
                    case 24:    return IM24x24;
                    case 32:    return IM32x32;
                    case 48:    return IM48x48;
                }
            }
            return getEmptyImage(width, height);
        } else {
            return Toolkit.getDefaultToolkit().getImage(url);
        }
    }
    
    /**
     * Get an image.
     * @param path Image path.
     * @param size Image size.
     * @return Image object.
     */
    public static Image getImage(String path, Dimension size) {
        return getImage(path, size.width, size.height);
    }
}
