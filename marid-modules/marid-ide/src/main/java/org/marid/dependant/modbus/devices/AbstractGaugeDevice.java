package org.marid.dependant.modbus.devices;

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

import eu.hansolo.medusa.Gauge;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Slider;
import org.marid.dependant.modbus.codec.ModbusCodec;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static java.lang.Double.parseDouble;
import static java.util.Optional.of;

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

    @Override
    public void loadFrom(Document document, Element element) {
        super.loadFrom(document, element);
        of(element.getAttribute("value")).filter(s -> !s.isEmpty()).ifPresent(v -> slider.setValue(parseDouble(v)));
    }

    @Override
    public void writeTo(Document document, Element element) {
        super.writeTo(document, element);
        element.setAttribute("value", Double.toString(slider.getValue()));
    }
}
