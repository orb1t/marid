/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.marid.ide.common;

import com.google.common.primitives.Ints;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;

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

	static Rectangle rect(int hash, int size) {
		return new Rectangle(size, size, color(hash));
	}

	static Path javaFile(int hash, int size) {
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
