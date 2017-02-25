/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
 * Marid, the free data acquisition and visualization software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.marid.dependant.modbus.devices;

import eu.hansolo.medusa.Gauge;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import org.marid.dependant.modbus.ModbusPane;
import org.marid.dependant.modbus.codec.CodecManager;
import org.marid.dependant.modbus.devices.info.AbstractDeviceInfo;
import org.marid.jfx.converter.MaridConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;

import javax.annotation.PostConstruct;
import java.util.Optional;
import java.util.function.DoubleFunction;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static org.marid.jfx.icons.FontIcon.D_CLOSE_BOX;
import static org.marid.jfx.icons.FontIcon.D_TOOLTIP_EDIT;
import static org.marid.jfx.icons.FontIcons.glyphIcon;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.9
 */
public abstract class AbstractDevice<I extends AbstractDeviceInfo> extends VBox {

    protected final HBox titleBox;
    protected final VBox bottomBox;
    protected final HBox addressBox;
    protected final Spinner<Integer> address;
    protected final ComboBox<Pair<String, DoubleFunction<byte[]>>> codecs;
    protected final TextField title;
    protected final Button editButton;
    protected final Button closeButton;
    protected final Slider slider;
    protected final Gauge gauge;

    public AbstractDevice(Gauge gauge) {
        super(5);
        setBackground(new Background(new BackgroundFill(new Color(0.5, 0.5, 0.5, 0.2), null, null)));
        getChildren().add(titleBox = new HBox(4,
                title = new TextField(),
                editButton = new Button(),
                closeButton = new Button())
        );
        getChildren().add(this.gauge = gauge);
        getChildren().add(bottomBox = new VBox(4,
                slider = new Slider(),
                addressBox = new HBox(4,
                        address = new Spinner<>(),
                        codecs = new ComboBox<>()
                ))
        );
        setPadding(new Insets(5));
        titleBox.setAlignment(Pos.BASELINE_LEFT);
        slider.maxProperty().bindBidirectional(gauge.maxValueProperty());
        slider.minProperty().bindBidirectional(gauge.minValueProperty());
        slider.valueProperty().bindBidirectional(gauge.valueProperty());
        slider.majorTickUnitProperty().bindBidirectional(gauge.majorTickSpaceProperty());
        slider.setShowTickMarks(true);
        title.setText(s(getClass().getSimpleName()));
        editButton.setGraphic(glyphIcon(D_TOOLTIP_EDIT, 16));
        closeButton.setGraphic(glyphIcon(D_CLOSE_BOX, 16));
        closeButton.setOnAction(event -> ((ModbusPane) getParent()).getChildren().remove(this));
        VBox.setVgrow(gauge, Priority.ALWAYS);
        HBox.setHgrow(address, Priority.ALWAYS);
        HBox.setHgrow(title, Priority.ALWAYS);
    }

    @PostConstruct
    private void initAddress() {
        final SpinnerValueFactory<Integer> valueFactory = new IntegerSpinnerValueFactory(0, 65535, 0);
        valueFactory.setConverter(new MaridConverter<>(i -> format("%04X", i), s -> parseInt(s, 16)));
        address.setValueFactory(valueFactory);
        address.setEditable(true);
    }

    @Autowired
    private void initCodecs(CodecManager codecManager) {
        codecs.setItems(codecManager.getCodecs());
        codecs.setConverter(new MaridConverter<>(Pair::getKey));
        codecs.getSelectionModel().select(0);
    }

    @SuppressWarnings("unchecked")
    private I newInfo() {
        final ResolvableType type = ResolvableType.forClass(AbstractDevice.class, getClass());
        final ResolvableType arg = type.getGeneric(0);
        try {
            return (I) arg.getRawClass().newInstance();
        } catch (Exception x) {
            throw new IllegalStateException(x);
        }
    }

    public I getInfo() {
        final I info = newInfo();
        info.address = address.getValue();
        info.codec = Optional.ofNullable(codecs.getSelectionModel().getSelectedItem()).map(Pair::getKey).orElse(null);
        return info;
    }

    public void setInfo(I info) {
        address.getValueFactory().setValue(info.address);
        codecs.getItems().stream()
                .filter(p -> p.getKey().equals(info.codec))
                .findAny()
                .ifPresent(p -> codecs.getSelectionModel().select(p));
    }
}
