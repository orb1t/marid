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

import com.google.common.util.concurrent.AtomicDouble;
import javafx.geometry.Bounds;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.layout.Pane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class ScreenParentPane extends Pane {

    private final ScreenPane screenPane;

    @Autowired
    public ScreenParentPane(ScreenPane screenPane) {
        getChildren().add(this.screenPane = screenPane);
    }

    @EventListener
    private void centerScreenPane(ContextStartedEvent event) {
        final Bounds bounds = screenPane.getBoundsInParent();
        final double x = (getWidth() - bounds.getWidth()) / 2.0;
        final double y = (getHeight() - bounds.getHeight()) / 2.0;
        screenPane.setLayoutX(x);
        screenPane.setLayoutY(y);
    }

    @PostConstruct
    private void emulatePan() {
        final AtomicDouble x = new AtomicDouble();
        final AtomicDouble y = new AtomicDouble();
        final AtomicDouble lx = new AtomicDouble();
        final AtomicDouble ly = new AtomicDouble();
        addEventHandler(MouseDragEvent.DRAG_DETECTED, event -> {
            //event.consume();
            x.set(event.getX());
            y.set(event.getY());
            lx.set(screenPane.getLayoutX());
            ly.set(screenPane.getLayoutY());
            startFullDrag();
        });
        addEventHandler(MouseDragEvent.MOUSE_DRAG_OVER, event -> {
            //event.consume();
            screenPane.setLayoutX(lx.get() + event.getX() - x.get());
            screenPane.setLayoutY(ly.get() + event.getY() - y.get());
        });
        addEventHandler(MouseDragEvent.MOUSE_DRAG_RELEASED, event -> {
            //event.consume();
            screenPane.setLayoutX(lx.get() + event.getX() - x.get());
            screenPane.setLayoutY(ly.get() + event.getY() - y.get());
        });
    }
}
