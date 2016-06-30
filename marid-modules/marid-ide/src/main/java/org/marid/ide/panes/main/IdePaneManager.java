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

import javafx.application.Platform;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import org.marid.jfx.action.FxAction;
import org.marid.spring.action.MenuAction;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.marid.jfx.icons.FontIcon.D_BORDER_TOP;
import static org.marid.jfx.icons.FontIcon.D_EXIT_TO_APP;

/**
 * @author Dmitry Ovchinnikov
 */
@Configuration
public class IdePaneManager {

    @Bean
    @MenuAction
    public FxAction alwaysOnTopAction(ObjectFactory<IdePane> idePaneObjectFactory) {
        return new FxAction(null, "ops", "Window")
                .setText("Always on top")
                .setIcon(D_BORDER_TOP)
                .setSelected(false)
                .setEventHandler(event -> {
                    final Stage stage = (Stage) idePaneObjectFactory.getObject().getScene().getWindow();
                    final CheckMenuItem menuItem = (CheckMenuItem) event.getSource();
                    stage.setAlwaysOnTop(menuItem.isSelected());
                });
    }

    @Bean
    @MenuAction
    public FxAction exitAction() {
        return new FxAction(null, "x", "File")
                .setText("Exit")
                .setIcon(D_EXIT_TO_APP)
                .setEventHandler(event -> Platform.exit())
                .setAccelerator(KeyCombination.valueOf("F12"));
    }
}
