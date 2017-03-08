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

package org.marid.dependant.modbus.devices;

import eu.hansolo.medusa.Gauge;
import javafx.scene.paint.Color;
import org.marid.dependant.modbus.devices.info.BarometerInfo;
import org.marid.spring.annotation.PrototypeComponent;

import static eu.hansolo.medusa.Gauge.SkinType.KPI;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.9
 */
@PrototypeComponent
public class Barometer extends AbstractGaugeDevice<BarometerInfo> {

    public Barometer() {
        super(new Gauge(KPI), BarometerInfo.class);
        gauge.setForegroundBaseColor(new Color(0.5, 0.5, 0.5, 1.0));
        gauge.setBarColor(new Color(0.7, 0.8, 0.9, 1.0));
    }

    @Override
    public Class<BarometerEditor> getEditor() {
        return BarometerEditor.class;
    }
}
