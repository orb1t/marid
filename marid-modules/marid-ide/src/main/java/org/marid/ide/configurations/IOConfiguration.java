package org.marid.ide.configurations;

import org.marid.IdeDependants;
import org.marid.dependant.modbus.ModbusSourceConfiguration;
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
                .setEventHandler(event -> dependants.start(ModbusSourceConfiguration.class, context -> {
                    context.setId("modbus");
                    context.setDisplayName("MODBUS devices");
                }));
    }
}
