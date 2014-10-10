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

package org.marid.site.images;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.marid.io.FastArrayOutputStream;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * @author Dmitry Ovchinnikov
 */
public class ImageUtil {

    public static Image image(Device device, BufferedImage bufferedImage) {
        final FastArrayOutputStream os = new FastArrayOutputStream();
        try {
            ImageIO.write(bufferedImage, "PNG", os);
            return new Image(device, os.getSharedInputStream());
        } catch (IOException x) {
            throw new IllegalStateException(x);
        }
    }

    public static Image fit(Image original, Rectangle bounds) {
        while (true) {
            final Rectangle b = original.getBounds();
            if (b.width <= bounds.width && b.height <= bounds.height) {
                return original;
            } else if (b.width > bounds.width && b.height > bounds.height) {
                final int height = (b.height * bounds.width) / b.width;
                original = new Image(original.getDevice(), original.getImageData().scaledTo(bounds.width, height));
            } else if (b.width > bounds.width) {
                final int height = (b.height * bounds.width) / b.width;
                return new Image(original.getDevice(), original.getImageData().scaledTo(bounds.width, height));
            } else {
                final int width = (b.width * bounds.height) / b.height;
                return new Image(original.getDevice(), original.getImageData().scaledTo(width, bounds.height));
            }
        }
    }
}
