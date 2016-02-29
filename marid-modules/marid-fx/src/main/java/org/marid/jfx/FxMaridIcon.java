/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
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

package org.marid.jfx;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.marid.image.MaridIcon;
import org.marid.io.FastArrayOutputStream;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * @author Dmitry Ovchinnikov
 */
public interface FxMaridIcon {

    static Image maridIcon(int size, Color color) {
        final java.awt.Color awtColor = new java.awt.Color(
                (float) color.getRed(),
                (float) color.getGreen(),
                (float) color.getBlue(),
                (float) color.getOpacity()
        );
        final BufferedImage image = MaridIcon.getImage(size, awtColor);
        final FastArrayOutputStream fos = new FastArrayOutputStream(size * size * 16);
        try {
            ImageIO.write(image, "PNG", fos);
        } catch (IOException x) {
            throw new IllegalStateException(x); // impossible
        }
        return new Image(fos.getSharedInputStream());
    }
}
