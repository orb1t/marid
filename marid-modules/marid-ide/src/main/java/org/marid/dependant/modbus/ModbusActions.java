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

package org.marid.dependant.modbus;

import javafx.beans.binding.Bindings;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import org.marid.dependant.modbus.annotation.Modbus;
import org.marid.dependant.modbus.devices.Barometer;
import org.marid.dependant.modbus.devices.Thermometer;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.icons.FontIcon;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

import static org.marid.jfx.action.Dialogs.overrideExt;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
@Configuration
public class ModbusActions {

    @Bean
    @Modbus
    public FxAction saveDevices(ModbusPane modbusPane, @Modbus File baseDir, @Modbus ObjectFactory<Stage> stage) {
        return new FxAction("ops", "ops", "File")
                .bindText("Save")
                .setIcon(FontIcon.D_CONTENT_SAVE)
                .setEventHandler(event -> {
                    final FileChooser fileChooser = new FileChooser();
                    fileChooser.setInitialDirectory(baseDir);
                    fileChooser.setTitle(s("Devices"));
                    fileChooser.getExtensionFilters().add(new ExtensionFilter("Device", "*.xml"));
                    final File file = overrideExt(fileChooser.showSaveDialog(stage.getObject()), ".xml");
                    if (file != null) {
                        modbusPane.save(file);
                    }
                })
                .bindDisabled(Bindings.isEmpty(modbusPane.getChildren()));
    }

    @Bean
    @Modbus
    public FxAction loadDevices(ModbusPane modbusPane, @Modbus File baseDir, @Modbus ObjectFactory<Stage> stage) {
        return new FxAction("ops", "ops", "File")
                .bindText("Open")
                .setIcon(FontIcon.D_BOOK_OPEN)
                .setEventHandler(event -> {
                    final FileChooser fileChooser = new FileChooser();
                    fileChooser.setInitialDirectory(baseDir);
                    fileChooser.setTitle(s("Devices"));
                    fileChooser.getExtensionFilters().add(new ExtensionFilter("Device", "*.xml"));
                    final File file = fileChooser.showOpenDialog(stage.getObject());
                    if (file != null) {
                        modbusPane.load(file);
                    }
                });
    }

    @Bean
    @Modbus
    public FxAction clearAll(ModbusPane modbusPane) {
        return new FxAction("remove", "ops", "Edit")
                .bindText("Clear All")
                .setIcon(FontIcon.M_CLEAR_ALL)
                .setEventHandler(event -> modbusPane.getChildren().clear())
                .bindDisabled(Bindings.isEmpty(modbusPane.getChildren()));
    }

    @Bean
    @Modbus
    public FxAction addThermometer(ModbusPane modbusPane, ObjectFactory<Thermometer> thermometer) {
        return new FxAction("devices", "devices", "Library")
                .bindText("Thermometer")
                .setIcon(FontIcon.W_THERMOMETER)
                .setEventHandler(event -> modbusPane.getChildren().add(thermometer.getObject()));
    }

    @Bean
    @Modbus
    public FxAction addBarometer(ModbusPane modbusPane, ObjectFactory<Barometer> barometer) {
        return new FxAction("devices", "devices", "Library")
                .bindText("Barometer")
                .setIcon(FontIcon.W_BAROMETER)
                .setEventHandler(event -> modbusPane.getChildren().add(barometer.getObject()));
    }
}
