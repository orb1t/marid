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

package org.marid.ide.settings.editors;

import org.marid.ide.settings.AbstractSettings;
import org.marid.ide.settings.DebugSettings;
import org.marid.jfx.panes.GenericGridPane;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * @author Dmitry Ovchinnikov
 */
@Dependent
public class DebugTab extends GenericGridPane implements SettingsEditor {

    private final DebugSettings debugSettings;

    @Inject
    public DebugTab(DebugSettings debugSettings) {
        this.debugSettings = debugSettings;
        addBooleanField("Debug", debugSettings, "debug");
        addIntField("Port", debugSettings, "port", 1000, 65535, 1);
        addBooleanField("Suspend", debugSettings, "suspend");
    }

    @Override
    public AbstractSettings getSettings() {
        return debugSettings;
    }
}
