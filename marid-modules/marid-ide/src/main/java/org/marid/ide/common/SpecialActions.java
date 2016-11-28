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

package org.marid.ide.common;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import org.intellij.lang.annotations.MagicConstant;
import org.marid.jfx.action.FxAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class SpecialActions {

    private final ApplicationContext context;

    @Autowired
    public SpecialActions(ApplicationContext context) {
        this.context = context;
    }

    public void set(@MagicConstant(valuesFromClass = SpecialActionConfiguration.class) String action,
                    Node node,
                    EventHandler<ActionEvent> eventHandler) {
        node.focusedProperty().addListener((observable, oldValue, newValue) -> {
            final FxAction fxAction = context.getBean(action, FxAction.class);
            if (newValue) {
                fxAction.setDisabled(false);
                fxAction.setEventHandler(eventHandler);
            } else {
                fxAction.setEventHandler(event -> {});
                fxAction.setDisabled(true);
            }
        });
    }
}
