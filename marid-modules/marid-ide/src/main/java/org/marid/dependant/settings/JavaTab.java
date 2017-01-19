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

package org.marid.dependant.settings;

import org.marid.ide.settings.JavaSettings;
import org.marid.jfx.panes.GenericGridPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.marid.jfx.props.Props.value;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class JavaTab extends GenericGridPane implements SettingsEditor {

    private final JavaSettings pref;

    @Autowired
    public JavaTab(JavaSettings pref) throws Exception {
        this.pref = pref;
        addTextField("Java executable", value(pref::getJavaExecutable, pref::setJavaExecutable));
    }

    @Override
    public JavaSettings getSettings() {
        return pref;
    }
}
