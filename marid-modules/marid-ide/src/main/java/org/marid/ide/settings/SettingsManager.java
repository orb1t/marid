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

import org.marid.IdeDependants;
import org.marid.dependant.settings.SettingsConfiguration;
import org.marid.jfx.action.FxAction;
import org.marid.spring.annotation.IdeAction;
import org.springframework.stereotype.Component;

import static org.marid.jfx.LocalizedStrings.ls;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class SettingsManager {

    @IdeAction
    public FxAction settingsAction(IdeDependants dependants) {
        return new FxAction("settings", "settings", "Tools")
                .setIcon("O_SETTINGS")
                .bindText(ls("Settings..."))
                .setEventHandler(event -> dependants.start(SettingsConfiguration.class, context -> {
                    context.setId("settingsEditor");
                    context.setDisplayName("Settings Editor");
                }));
    }
}
