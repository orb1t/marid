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

package org.marid.dependant.project.config;

import org.apache.maven.model.Model;
import org.marid.jfx.panes.GenericGridPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * @author Dmitry Ovchinnikov
 */
@Component("Properties")
@Qualifier("projectConf")
public class PropertiesTab extends GenericGridPane {

    @Autowired
    public PropertiesTab(Model model) {
        final Properties properties = model.getProperties();
        addTextField(properties, "Marid version", "marid.runtime.version", System.getProperty("implementation.version"));
    }

    private void addTextField(Properties properties, String text, String key, String defaultValue) {
        addTextField(text, () -> properties.getProperty(key, defaultValue), value -> properties.setProperty(key, value));
    }
}
