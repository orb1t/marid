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

package org.marid.idefx.controls;

import com.google.common.primitives.Ints;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import org.marid.ide.project.ProjectProfile;
import org.marid.spring.xml.BeanData;
import org.marid.spring.xml.DRef;

import java.nio.file.Path;
import java.util.Objects;

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

    static Circle ref(DRef ref, int size) {
        return new Circle(size / 2, color(Objects.hash(ref.getBean())));
    }

    static Circle ref(String ref, int size) {
        return new Circle(size / 2, color(Objects.hash(ref)));
    }

    static Circle beanNode(BeanData beanData, int size) {
        return new Circle(size / 2, color(Objects.hash(beanData.getName())));
    }

    static HBox beanNode(ProjectProfile profile, BeanData beanData, int size) {
        return new HBox(4, profileNode(profile, size), beanNode(beanData, size));
    }

    static Rectangle profileNode(ProjectProfile profile, int size) {
        return new Rectangle(size, size, color(Objects.hash(profile.getName())));
    }

    static Pane fileNode(Path path, int size) {
        final double h = size / Math.sqrt(2);
        final Rectangle node = new Rectangle(h, h, color(path.hashCode()));
        node.setRotate(45);
        return new StackPane(node);
    }

    static HBox fileNode(ProjectProfile profile, Path path, int size) {
        return new HBox(4, profileNode(profile, size), fileNode(path, size));
    }
}
