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

package org.marid.dependant.modbus.devices.infos;

import org.marid.dependant.modbus.devices.AbstractDevice;

import javax.xml.bind.annotation.*;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.9
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "devices")
@XmlSeeAlso({DeviceEntry.class})
public class DeviceInfos {

    @XmlElement
    public DeviceEntry[] entries;

    public DeviceInfos() {
    }

    public DeviceInfos(AbstractDevice<?>... devices) {
        entries = new DeviceEntry[devices.length];
        for (int i = 0; i < devices.length; i++) {
            final DeviceEntry entry = new DeviceEntry();
            entry.type = devices[i].getClass().getName();
            entry.info = devices[i].getInfo();
            entries[i] = entry;
        }
    }
}
