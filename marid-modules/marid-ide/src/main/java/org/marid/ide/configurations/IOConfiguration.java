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

package org.marid.ide.configurations;

import org.marid.dependant.modbus.ModbusSourceConfiguration;
import org.marid.ide.IdeDependants;
import org.marid.jfx.action.FxAction;
import org.marid.spring.annotation.IdeAction;
import org.springframework.stereotype.Component;

import static org.marid.jfx.LocalizedStrings.ls;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
@Component
public class IOConfiguration {

    @IdeAction
    public FxAction modbusAction(IdeDependants dependants) {
        return new FxAction("modbus", "I/O")
                .setIcon("M_DEVICES")
                .bindText(ls("MODBUS devices"))
                .setEventHandler(event -> dependants.run(c -> {
                    c.register(ModbusSourceConfiguration.class);
                    c.setDisplayName("MODBUS devices");
                }));
    }
}
