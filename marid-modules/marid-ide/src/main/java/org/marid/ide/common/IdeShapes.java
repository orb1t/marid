/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.marid.ide.common;

import javafx.scene.paint.Color;
import javafx.scene.shape.*;

import java.nio.ByteBuffer;

import static java.lang.Byte.toUnsignedInt;
import static java.util.stream.IntStream.range;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
public interface IdeShapes {

  static Color color(int hash) {
    final byte[] h = ByteBuffer.allocate(4).putInt(0, Integer.reverse(hash)).array();
    final double[] d = range(0, h.length).mapToDouble(i -> toUnsignedInt(h[i]) / 255.0).toArray();
    return new Color(d[0], d[1], d[2], 1 - d[3] / 2.0);
  }

  static Circle circle(int hash, int size) {
    return new Circle(size / 2, color(hash));
  }

  static Rectangle rect(int hash, int size) {
    return new Rectangle(size, size, color(hash));
  }

  static Path diamond(int hash, int size) {
    final Path path = new Path(
        new MoveTo(0, size / 2.0),
        new LineTo(size / 2.0, size),
        new LineTo(size, size / 2.0),
        new LineTo(size / 2.0, 0),
        new ClosePath()
    );
    final Color color = color(hash);
    path.setStroke(color.darker());
    path.setFill(color);
    return path;
  }
}
