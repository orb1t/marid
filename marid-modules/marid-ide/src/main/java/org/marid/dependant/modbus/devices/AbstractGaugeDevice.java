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
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Slider;
import org.marid.dependant.modbus.codec.ModbusCodec;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.9
 */
abstract class AbstractGaugeDevice extends AbstractDevice<Float> {

    final Gauge gauge;
    final Slider slider;

    AbstractGaugeDevice(Gauge gauge) {
        super(Float.class);
        setCenter(this.gauge = gauge);
        setRight(this.slider = new Slider());
        slider.setShowTickMarks(true);
        slider.setOrientation(Orientation.VERTICAL);
        slider.setPadding(new Insets(4));
        slider.maxProperty().bindBidirectional(gauge.maxValueProperty());
        slider.minProperty().bindBidirectional(gauge.minValueProperty());
        slider.valueProperty().bindBidirectional(gauge.valueProperty());
        slider.majorTickUnitProperty().bindBidirectional(gauge.majorTickSpaceProperty());
        gauge.setKeepAspect(true);
    }

    @Override
    public byte[] getData() {
        final ModbusCodec<Float> codec = this.codec.getValue();
        return codec.encode((float) gauge.getValue());
    }
}
