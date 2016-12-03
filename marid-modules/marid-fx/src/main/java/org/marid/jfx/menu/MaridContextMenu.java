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

package org.marid.jfx.menu;

import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.stage.Window;

import java.util.function.Consumer;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
public class MaridContextMenu extends ContextMenu {

    private final Consumer<MaridContextMenu> onPreShow;

    public MaridContextMenu(Consumer<MaridContextMenu> onPreShow) {
        this.onPreShow = onPreShow;
    }

    @Override
    public void show(Node anchor, double screenX, double screenY) {
        onPreShow.accept(this);
        super.show(anchor, screenX, screenY);
    }

    @Override
    public void show(Window owner) {
        onPreShow.accept(this);
        super.show(owner);
    }

    @Override
    public void show(Node anchor, Side side, double dx, double dy) {
        onPreShow.accept(this);
        super.show(anchor, side, dx, dy);
    }

    @Override
    public void show(Window ownerWindow, double anchorX, double anchorY) {
        onPreShow.accept(this);
        super.show(ownerWindow, anchorX, anchorY);
    }
}
