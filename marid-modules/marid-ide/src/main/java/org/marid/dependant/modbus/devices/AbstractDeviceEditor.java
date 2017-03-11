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

import com.digitalpetri.modbus.FunctionCode;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Spinner;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.marid.dependant.modbus.codec.ModbusCodec;
import org.marid.dependant.modbus.devices.info.AbstractDeviceInfo;
import org.marid.jfx.converter.MaridConverter;
import org.marid.jfx.panes.GenericGridPane;
import org.marid.jfx.panes.MaridScrollPane;

import javax.annotation.PostConstruct;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static javafx.collections.FXCollections.observableArrayList;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.9
 */
public class AbstractDeviceEditor<I extends AbstractDeviceInfo, E, T extends AbstractDevice<I, E>> extends Dialog<I> {

    protected final T device;
    protected final I info;
    protected final GenericGridPane table = new GenericGridPane();
    protected final Spinner<Integer> address;
    protected final ComboBox<ModbusCodec<E>> codecs;
    protected final ComboBox<FunctionCode> functions = new ComboBox<>(observableArrayList(FunctionCode.values()));

    public AbstractDeviceEditor(T device, Stage stage) {
        this.device = device;
        this.codecs = new ComboBox<>(device.codec.getItems());
        this.info = device.getInfo();
        this.address = new Spinner<>(0, 65535, info.address);
        initOwner(stage);
        initModality(Modality.WINDOW_MODAL);
        getDialogPane().getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);
        getDialogPane().setContent(new MaridScrollPane(table));
        setTitle(s(device.getClass().getSimpleName()));
        setResultConverter(t -> {
            switch (t.getButtonData()) {
                case APPLY:
                    accept();
                    device.setInfo(info);
                    return info;
                default:
                    return null;
            }
        });
        setResizable(true);
    }

    @PostConstruct
    private void initAddress() {
        address.getValueFactory().setConverter(new MaridConverter<>(i -> format("%04X", i), s -> parseInt(s, 16)));
        address.setEditable(true);
        table.addControl("Address", () -> address);
    }

    @PostConstruct
    private void initCodecs() {
        codecs.setConverter(new MaridConverter<>(ModbusCodec::getName));
        codecs.getSelectionModel().select(device.codec.getSelectionModel().getSelectedIndex());
        table.addControl("Codec", () -> codecs);
    }

    @PostConstruct
    private void initFuncs() {
        functions.getSelectionModel().select(info.function);
        table.addControl("Function", () -> functions);
    }

    protected void accept() {
        info.codec = codecs.getSelectionModel().getSelectedItem().getName();
        info.address = address.getValue();
        info.function = functions.getSelectionModel().getSelectedItem();
    }
}
