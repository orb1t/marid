/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.marid.ide.configurations;

import org.marid.dependant.modbus.ModbusSourceConfiguration;
import org.marid.ide.IdeDependants;
import org.marid.jfx.action.FxAction;
import org.marid.idelib.spring.annotation.IdeAction;
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
