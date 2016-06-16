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

package org.marid.ide.settings;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.marid.Ide;
import org.marid.dependant.settings.SettingsDialog;
import org.marid.ide.menu.IdeMenuItem;
import org.marid.ide.toolbar.IdeToolbarItem;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.marid.jfx.icons.FontIcon.O_SETTINGS;

/**
 * @author Dmitry Ovchinnikov
 */
@Configuration
public class SettingsManager {

    @Bean
    @IdeToolbarItem(group = "settings")
    @IdeMenuItem(menu = "Tools", text = "Settings...", group = "settings", icon = O_SETTINGS)
    public EventHandler<ActionEvent> settingsItem() {
        return event -> {
            final SettingsDialog settingsDialog = Ide.newDialog(SettingsDialog.class);
            settingsDialog.showAndWait();
        };
    }
}
