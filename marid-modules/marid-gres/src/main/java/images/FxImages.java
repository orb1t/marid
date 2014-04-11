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

import javafx.geometry.Dimension2D;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Dmitry Ovchinnikov.
 */
public class FxImages {

    public static Image getEmptyImage(Dimension2D dimension) {
        return getEmptyImage((int) dimension.getWidth(), (int) dimension.getHeight());
    }

    public static Image getImage(int size) {
        return getEmptyImage(size, size);
    }

    public static Image getEmptyImage(int width, int height) {
        return new WritableImage(width, height);
    }

    public static Image getImage(String path, int width, int height) {
        try (final InputStream inputStream = FxImages.class.getResourceAsStream(path)) {
            return new Image(inputStream, width, height, false, true);
        } catch (IOException x) {
            throw new IllegalStateException(x);
        }
    }
}
