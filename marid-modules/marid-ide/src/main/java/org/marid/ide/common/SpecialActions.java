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
import javafx.scene.input.KeyCombination;
import org.marid.jfx.action.FxAction;
import org.marid.spring.action.IdeAction;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.marid.jfx.icons.FontIcon.M_FOLDER_SHARED;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
@Configuration
public class SpecialActions {

    @Bean
    @IdeAction
    @Qualifier("profile")
    public FxAction editAction() {
        return new FxAction("projectTree", "pt", "Project")
                .setText(s("Project resources"))
                .setAccelerator(KeyCombination.valueOf("F4"))
                .setIcon(M_FOLDER_SHARED)
                .setDisabled(true);
    }


    public void setEditAction(String text, EventHandler<ActionEvent> eventHandler) {
        if (text == null || eventHandler == null) {
            editAction().setText(s("Edit..."));
            editAction().setEventHandler(event -> {});
            editAction().setDisabled(true);
        } else {
            editAction().setDisabled(false);
            editAction().setEventHandler(eventHandler);
            editAction().setText(text);
        }
    }
}
