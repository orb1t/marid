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
import javafx.scene.paint.Color;
import org.marid.dependant.modbus.annotation.DeviceIcon;
import org.marid.spring.annotation.PrototypeComponent;

import static eu.hansolo.medusa.Gauge.SkinType.KPI;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.9
 */
@PrototypeComponent("Barometer")
@DeviceIcon("W_BAROMETER")
public class Barometer extends AbstractGaugeDevice {

    public Barometer() {
        super(new Gauge(KPI));
        gauge.setForegroundBaseColor(new Color(0.5, 0.5, 0.5, 1.0));
        gauge.setBarColor(new Color(0.7, 0.8, 0.9, 1.0));
    }

    @Override
    public Class<BarometerEditor> getEditor() {
        return BarometerEditor.class;
    }
}
