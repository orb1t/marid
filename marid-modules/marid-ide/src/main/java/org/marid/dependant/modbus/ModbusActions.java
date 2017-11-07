/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.marid.dependant.modbus;

import javafx.beans.binding.Bindings;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import org.marid.dependant.modbus.annotation.Modbus;
import org.marid.dependant.modbus.repo.ModbusConfig;
import org.marid.dependant.modbus.repo.ModbusService;
import org.marid.jfx.action.FxAction;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.File;

import static org.marid.jfx.LocalizedStrings.fls;
import static org.marid.jfx.action.Dialogs.overrideExt;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
@Component
public class ModbusActions {

	@Bean
	@Modbus
	public FxAction saveDevices(ModbusPane modbusPane, @Modbus File baseDir, @Modbus ObjectFactory<Stage> stage) {
		return new FxAction("ops", "ops", "File")
				.bindText("Save")
				.setIcon("D_CONTENT_SAVE")
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
				.setIcon("D_BOOK_OPEN")
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
				.setIcon("M_CLEAR_ALL")
				.setEventHandler(event -> modbusPane.getChildren().clear())
				.bindDisabled(Bindings.isEmpty(modbusPane.getChildren()));
	}

	@Bean
	@Modbus
	public FxAction runAction(ModbusService service) {
		return new FxAction("run", "service", "Service")
				.bindText("Run")
				.setIcon("D_PLAY")
				.setEventHandler(event -> service.start())
				.bindDisabled(service.activeProperty());
	}

	@Bean
	@Modbus
	public FxAction stopAction(ModbusService service) {
		return new FxAction("run", "service", "Service")
				.bindText("Stop")
				.setIcon("D_STOP")
				.setEventHandler(event -> service.stop())
				.bindDisabled(service.activeProperty().not());
	}

	@Bean(initMethod = "run")
	public Runnable init(@Modbus ToolBar topToolbar, ModbusConfig config) {
		return () -> {
			final Label hostLabel = new Label();
			hostLabel.textProperty().bind(fls("%s: ", "Host"));

			final TextField hostField = new TextField();
			hostField.textProperty().bindBidirectional(config.host);

			final Label portLabel = new Label();
			portLabel.textProperty().bind(fls("  %s: ", "Port"));

			final Spinner<Number> portSpinner = new Spinner<>(0, 65535, config.port.get());
			portSpinner.getValueFactory().valueProperty().bindBidirectional(config.port);
			portSpinner.setEditable(true);

			topToolbar.getItems().addAll(hostLabel, hostField, portLabel, portSpinner);
		};
	}
}
