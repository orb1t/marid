package org.marid.dependant.modbus.devices;

import javafx.stage.Stage;
import org.marid.spring.annotation.PrototypeComponent;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.9
 */
@PrototypeComponent
public class BarometerEditor extends AbstractDeviceEditor<Float, Barometer> {

    public BarometerEditor(Barometer device, Stage stage) {
        super(device, stage);
    }
}
