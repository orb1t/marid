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

package org.marid.ide.panes.main;

import javafx.scene.Node;
import javafx.scene.control.ToolBar;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.action.MaridActions;
import org.marid.spring.action.IdeAction;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class IdeToolbar extends ToolBar {

    private final ObjectFactory<Map<String, FxAction>> menuActionsFactory;

    @Autowired
    public IdeToolbar(@IdeAction ObjectFactory<Map<String, FxAction>> menuActionsFactory) {
        this.menuActionsFactory = menuActionsFactory;
    }

    @EventListener
    private void onIdeStart(ContextStartedEvent event) {
        getItems().addAll(MaridActions.toolbar(menuActionsFactory.getObject()));
    }

    public void on(Node node, Supplier<Map<String, FxAction>> actionMapSupplier) {
        final List<Node> nodes = new ArrayList<>();
        node.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                Collections.addAll(nodes, MaridActions.toolbar(actionMapSupplier.get()));
                getItems().addAll(nodes);
            } else {
                getItems().removeAll(nodes);
                nodes.clear();
            }
        });
    }
}
