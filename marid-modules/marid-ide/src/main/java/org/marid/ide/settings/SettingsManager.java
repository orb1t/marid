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

import de.jensd.fx.glyphs.octicons.OctIcon;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.marid.ide.menu.IdeMenuItem;
import org.marid.ide.settings.editors.SettingsDialog;
import org.marid.ide.toolbar.IdeToolbarItem;
import org.marid.pref.PrefSupport;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Provider;

/**
 * @author Dmitry Ovchinnikov
 */
@ApplicationScoped
public class SettingsManager implements PrefSupport {

    @Produces
    @IdeToolbarItem(group = "settings")
    @IdeMenuItem(menu = "Tools", text = "Settings...", group = "settings", oIcons = {OctIcon.SETTINGS})
    public EventHandler<ActionEvent> settingsItem(Provider<SettingsDialog> settingsDialogProvider) {
        return event -> {
            final SettingsDialog settingsDialog = settingsDialogProvider.get();
            settingsDialog.showAndWait().ifPresent(holder -> holder.save(settingsDialog.preferences()));
        };
    }
}
