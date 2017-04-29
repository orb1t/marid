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

import javafx.beans.binding.BooleanBinding;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;
import org.apache.maven.model.Model;
import org.marid.jfx.panes.GenericGridPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Properties;

import static org.marid.jfx.props.Props.value;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
@Qualifier("projectConf")
@Order(2)
public class PropertiesTab extends GenericGridPane {

    private final Properties properties;

    @Autowired
    public PropertiesTab(Model model) {
        setId("Properties");
        properties = model.getProperties();
    }

    @Order(1)
    @Autowired
    public void initMaridVersion(@Value("${implementation.version}") String version) {
        addTextField("Marid version", value(
                () -> properties.getProperty("marid.version", version),
                v -> properties.setProperty("marid.version", v)));
        addSeparator();
    }

    @Order(2)
    @Autowired
    public void initDebug() {
        final CheckBox debugCheckBox = addBooleanField("Debug",
                () -> "true".equals(properties.getProperty("marid.debug")),
                v -> {
                    if (v) {
                        properties.setProperty("marid.debug", "true");
                    } else {
                        properties.remove("marid.debug");
                    }
                });
        final CheckBox debugServerCheckBox = addBooleanField("Server mode",
                () -> "true".equals(properties.getProperty("marid.debug.server", "true")),
                v -> {
                    if (v) {
                        properties.remove("marid.debug.server");
                    } else {
                        properties.setProperty("marid.debug.server", "n");
                    }
                });
        final CheckBox debugSuspendCheckBox = addBooleanField("Suspend mode",
                () -> "true".equals(properties.getProperty("marid.debug.suspend")),
                v -> {
                    if (v) {
                        properties.setProperty("marid.debug.suspend", "y");
                    } else {
                        properties.remove("marid.debug.suspend");
                    }
                });
        final Spinner<Integer> debugPortSpinner = addIntField("Debug port",
                () -> Integer.parseInt(properties.getProperty("marid.debug.port", "5005")),
                v -> {
                    if (v == 5005) {
                        properties.remove("marid.debug.port");
                    } else {
                        properties.setProperty("marid.debug.port", Integer.toString(v));
                    }
                }, 5000, 65535, 1);
        final Spinner<Integer> debugTimeout = addIntField("Debug socket timeout",
                () -> Integer.parseInt(properties.getProperty("marid.debug.timeout", "30000")) / 1000,
                v -> {
                    if (v == 30) {
                        properties.remove("marid.debug.timeout");
                    } else {
                        properties.setProperty("marid.debug.timeout", Integer.toString(v * 1000));
                    }
                }, 1, 180, 1);
        final BooleanBinding disabled = debugCheckBox.selectedProperty().not();
        debugServerCheckBox.disableProperty().bind(disabled);
        debugSuspendCheckBox.disableProperty().bind(disabled);
        debugPortSpinner.disableProperty().bind(disabled);
        debugTimeout.disableProperty().bind(disabled);
    }
}
