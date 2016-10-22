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

package org.marid.editors.hmi.screen;

import javafx.scene.Group;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Box;
import javafx.scene.shape.Circle;
import javafx.scene.transform.Affine;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class ScreenPane extends Group {

    private final Affine currentTransform = new Affine();

    public ScreenPane() {
        final Circle circle = new Circle(100, 100, 100);
        circle.addEventHandler(MouseEvent.DRAG_DETECTED, event -> {
            circle.startFullDrag();
        });
        circle.addEventHandler(MouseDragEvent.MOUSE_DRAG_OVER, event -> {
            circle.setCenterX(event.getY());
            circle.setCenterY(event.getY());
        });
        getChildren().add(circle);
        final Box box = new Box(50, 50, 2);
        getChildren().add(box);
        getTransforms().add(currentTransform);
    }

    @PostConstruct
    private void emulateZoom() {
        setOnScroll(event -> {
            if (!event.isShiftDown()) {
                return;
            }
            final double scale = event.getDeltaY() > 0 ? 1.1 : 0.9;
            currentTransform.appendScale(scale, scale, event.getX(), event.getY());
        });
    }
}
