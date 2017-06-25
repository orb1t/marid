package org.marid.dependant.modbus.devices;

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
