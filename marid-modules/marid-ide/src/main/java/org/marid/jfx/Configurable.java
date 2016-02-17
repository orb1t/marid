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

import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.marid.pref.PrefSupport;

/**
 * @author Dmitry Ovchinnikov
 */
public interface Configurable extends PrefSupport {

    default void initBounds(Stage stage) {
        stage.setMaximized(preferences().getBoolean("max", false));
        if (!stage.isMaximized()) {
            stage.setWidth(preferences().getDouble("w", Math.min(stage.getMinWidth(), 800d)));
            stage.setHeight(preferences().getDouble("h", Math.min(stage.getMinHeight(), 600d)));
            stage.setX(preferences().getDouble("x", 0));
            stage.setY(preferences().getDouble("y", 0));
        }
        stage.addEventHandler(WindowEvent.WINDOW_HIDDEN, event -> {
            preferences().putBoolean("max", stage.isMaximized());
            if (!stage.isMaximized()) {
                preferences().putDouble("w", stage.getWidth());
                preferences().putDouble("h", stage.getHeight());
                preferences().putDouble("x", stage.getX());
                preferences().putDouble("y", stage.getY());
            }
        });
    }
}
