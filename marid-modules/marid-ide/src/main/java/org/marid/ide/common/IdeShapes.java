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

package org.marid.ide.common;

import com.google.common.primitives.Ints;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import static java.lang.Byte.toUnsignedInt;
import static java.util.stream.IntStream.range;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
public interface IdeShapes {

    static Color color(int hash) {
        final byte[] h = Ints.toByteArray(Integer.reverse(hash));
        final double[] d = range(0, h.length).mapToDouble(i -> toUnsignedInt(h[i]) / 255.0).toArray();
        return new Color(d[0], d[1], d[2], 1 - d[3] / 2.0);
    }

    static Circle circle(int hash, int size) {
        return new Circle(size / 2, color(hash));
    }
}
