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

package org.marid.ide.menu;

import javafx.event.EventHandler;
import javafx.stage.WindowEvent;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.menu.MaridMenu;
import org.marid.spring.action.IdeAction;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static javafx.stage.WindowEvent.WINDOW_SHOWING;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class IdeMenu extends MaridMenu {

    @Autowired
    public IdeMenu(@IdeAction ObjectFactory<Map<String, FxAction>> menuActionsFactory) {
        sceneProperty().addListener((s, os, ns) -> {
            if (ns != null) {
                final Map<String, FxAction> actionMap = menuActionsFactory.getObject();
                final EventHandler<WindowEvent> handler = event -> init(actionMap);
                ns.windowProperty().addListener((w, ow, nw) -> nw.addEventHandler(WINDOW_SHOWING, handler));
            }
        });
    }
}
