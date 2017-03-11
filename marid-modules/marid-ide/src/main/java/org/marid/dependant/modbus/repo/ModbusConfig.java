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

package org.marid.dependant.modbus.repo;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.marid.dependant.modbus.devices.infos.DeviceInfos;
import org.springframework.stereotype.Repository;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.9
 */
@Repository
public class ModbusConfig {

    public final StringProperty host = new SimpleStringProperty("0.0.0.0");
    public final IntegerProperty port = new SimpleIntegerProperty(10502);

    public void initDevices(DeviceInfos deviceInfos) {
        deviceInfos.host = host.get();
        deviceInfos.port = port.get();
    }

    public void restoreDevices(DeviceInfos deviceInfos) {
        host.set(deviceInfos.host);
        port.set(deviceInfos.port);
    }
}
