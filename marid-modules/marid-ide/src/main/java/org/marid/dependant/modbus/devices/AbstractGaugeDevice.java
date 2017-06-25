package org.marid.dependant.modbus.devices;

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
