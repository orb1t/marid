/*
 *
 */

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
import eu.hansolo.medusa.LcdDesign;
import javafx.scene.paint.Color;
import org.marid.dependant.modbus.annotation.DeviceIcon;
import org.marid.spring.annotation.PrototypeComponent;

import static eu.hansolo.medusa.Gauge.SkinType.LCD;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.9
 */
@PrototypeComponent
@DeviceIcon("W_THERMOMETER")
public class Thermometer extends AbstractGaugeDevice {

    public Thermometer() {
        super(new Gauge(LCD));
        gauge.setLcdDesign(LcdDesign.LIGHTGREEN_BLACK);
        gauge.setDecimals(1);
        gauge.setTickLabelDecimals(1);
        gauge.setMinMeasuredValueVisible(true);
        gauge.setMaxMeasuredValueVisible(true);
        gauge.setOldValueVisible(true);
        gauge.setBorderPaint(Color.WHITE);
        gauge.setForegroundPaint(Color.WHITE);
    }

    @Override
    public Class<ThermometerEditor> getEditor() {
        return ThermometerEditor.class;
    }
}
