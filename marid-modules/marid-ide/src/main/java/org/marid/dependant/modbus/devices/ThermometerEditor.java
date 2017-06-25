package org.marid.dependant.modbus.devices;

import javafx.stage.Stage;
import org.marid.spring.annotation.PrototypeComponent;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.9
 */
@PrototypeComponent
public class ThermometerEditor extends AbstractDeviceEditor<Float, Thermometer> {

    public ThermometerEditor(Thermometer device, Stage stage) {
        super(device, stage);
    }
}
