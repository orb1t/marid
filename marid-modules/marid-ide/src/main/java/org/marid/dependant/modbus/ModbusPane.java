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
import javafx.geometry.Insets;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import org.marid.dependant.modbus.devices.AbstractDevice;
import org.marid.dependant.modbus.devices.info.AbstractDeviceInfo;
import org.marid.dependant.modbus.devices.infos.DeviceEntry;
import org.marid.dependant.modbus.devices.infos.DeviceInfos;
import org.marid.dependant.modbus.repo.ModbusConfig;
import org.marid.misc.Casts;
import org.marid.xml.XmlBind;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;

import static java.util.logging.Level.WARNING;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
@Component
public class ModbusPane extends FlowPane {

    private final AutowireCapableBeanFactory factory;
    private final ModbusConfig config;

    @Autowired
    public ModbusPane(AutowireCapableBeanFactory factory, ModbusConfig config) {
        super(10, 10);
        this.factory = factory;
        this.config = config;
        setPadding(new Insets(10));
    }

    public void save(File file) {
        final DeviceInfos infos = new DeviceInfos(getChildren().stream()
                .filter(AbstractDevice.class::isInstance)
                .map(AbstractDevice.class::cast)
                .toArray(AbstractDevice[]::new));
        config.initDevices(infos);
        try {
            XmlBind.save(infos, file, XmlBind.FORMATTED_OUTPUT, Marshaller::marshal);
            initTitle(file);
        } catch (Exception x) {
            log(WARNING, "Unable to save {0}", x, file);
        }
    }

    public void load(File file) {
        try {
            final DeviceInfos infos = XmlBind.load(DeviceInfos.class, file, Unmarshaller::unmarshal);
            getChildren().clear();
            for (final DeviceEntry entry : infos.entries) {
                final Class<?> c = Class.forName(entry.type);
                final AbstractDevice<?, ?> bean = (AbstractDevice<?, ?>) factory.createBean(c);
                bean.setInfo(Casts.cast(entry.info));
                getChildren().add(bean);
            }
            initTitle(file);
            config.restoreDevices(infos);
        } catch (Exception x) {
            log(WARNING, "Unable to load {0}", x, file);
        }
    }

    public <I extends AbstractDeviceInfo, T> void add(AbstractDevice<I, T> device) {
        getChildren().add(device);
    }

    private void initTitle(File file) {
        if (getScene() == null) {
            return;
        }
        if (getScene().getWindow() == null) {
            return;
        }
        final Stage stage = (Stage) getScene().getWindow();
        stage.titleProperty().bind(Bindings.createObjectBinding(() -> String.format("MODBUS: %s", file.getName())));
    }
}
