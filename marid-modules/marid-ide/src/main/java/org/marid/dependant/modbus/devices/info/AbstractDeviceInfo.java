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

package org.marid.dependant.modbus.devices.info;

import com.digitalpetri.modbus.FunctionCode;
import org.marid.dependant.modbus.codec.FloatCodec;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.9
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlSeeAlso({
        BarometerInfo.class,
        ThermometerInfo.class
})
public abstract class AbstractDeviceInfo {

    @XmlAttribute
    public int address = 0;

    @XmlAttribute
    public String codec = new FloatCodec().getName();

    @XmlAttribute
    public FunctionCode function = FunctionCode.ReadHoldingRegisters;
}