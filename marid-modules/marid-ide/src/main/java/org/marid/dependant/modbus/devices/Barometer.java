package org.marid.dependant.modbus.devices;

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
