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

package org.marid.ide.project.editors;

import javafx.beans.property.StringProperty;
import org.apache.maven.model.Model;
import org.marid.jfx.Props;
import org.marid.jfx.panes.AbstractGridPane;

import java.util.Properties;

/**
 * @author Dmitry Ovchinnikov
 */
public class PropertiesTab extends AbstractGridPane {

    public PropertiesTab(Model model) {
        final Properties properties = model.getProperties();
        addTextField(properties, "Marid version", "marid.runtime.version", System.getProperty("implementation.version"));
    }

    private void addTextField(Properties properties, String text, String key, String defaultValue) {
        addTextField(text, stringProperty(properties, key, defaultValue));
    }

    private StringProperty stringProperty(Properties properties, String key, String defaultValue) {
        return Props.stringProperty(() -> properties.getProperty(key, defaultValue), v -> properties.setProperty(key, v));
    }
}
