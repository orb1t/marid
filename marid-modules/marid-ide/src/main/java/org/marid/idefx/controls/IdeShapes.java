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

import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import org.marid.ide.project.ProjectProfile;
import org.marid.spring.xml.BeanData;
import org.marid.spring.xml.DRef;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Objects;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
public interface IdeShapes {

    static Color color(int hash) {
        final ByteBuffer buffer = ByteBuffer.allocate(4).putInt(0, hash);
        final double red = Byte.toUnsignedInt(buffer.get(0)) / 255.0;
        final double green = Byte.toUnsignedInt(buffer.get(1)) / 255.0;
        final double blue = Byte.toUnsignedInt(buffer.get(2)) / 255.0;
        final double opacity = Byte.toUnsignedInt(buffer.get(3)) / (2.0 * 255.0);
        return new Color(1 - red, 1 - green, 1 - blue, 1 - opacity);
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
