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

package org.marid.ide.panes.cmd;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.TextArea;
import javafx.scene.layout.TilePane;
import org.marid.ee.IdeSingleton;
import org.marid.ide.menu.IdeMenuItem;
import org.marid.ide.toolbar.IdeToolbarItem;

import javax.enterprise.inject.Produces;

/**
 * @author Dmitry Ovchinnikov
 */
@IdeSingleton
public class CmdPane extends TilePane {

    public CmdPane() {
        super(Orientation.VERTICAL, 10.0, 10.0);
        setPadding(new Insets(10.0, 10.0, 10.0, 10.0));
        setPrefTileWidth(Double.MAX_VALUE);
        setMinWidth(0.0);
        setMaxWidth(Double.MAX_VALUE);
        setStyle("-fx-background-color: -fx-mid-text-color");
    }

    @Produces
    @IdeToolbarItem(group = "cmd")
    @IdeMenuItem(menu = "Commands", text = "Clear result area", group = "cmd", faIcons = {FontAwesomeIcon.REMOVE})
    public EventHandler<ActionEvent> clearResultAreaCommand() {
        return event -> getChildren().removeIf(n -> !(n instanceof TextArea));
    }
}
