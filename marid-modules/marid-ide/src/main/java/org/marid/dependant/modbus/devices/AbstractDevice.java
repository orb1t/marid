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

package org.marid.dependant.modbus.devices;

import com.digitalpetri.modbus.FunctionCode;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.marid.dependant.modbus.ModbusPane;
import org.marid.dependant.modbus.annotation.Modbus;
import org.marid.dependant.modbus.codec.CodecManager;
import org.marid.dependant.modbus.codec.ModbusCodec;
import org.marid.jfx.converter.MaridConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.annotation.PostConstruct;

import static javafx.collections.FXCollections.observableArrayList;
import static org.marid.jfx.LocalizedStrings.ls;
import static org.marid.jfx.icons.FontIcons.glyphIcon;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.9
 */
public abstract class AbstractDevice<T> extends BorderPane {

    final Class<T> type;
    final HBox titleBox;
    final TextField title;
    final Button editButton;
    final Button closeButton;
    final HBox addressBox;
    final Label address;
    final ComboBox<FunctionCode> functions;
    final ComboBox<ModbusCodec<T>> codec;

    AbstractDevice(Class<T> type) {
        this.type = type;
        setBackground(new Background(new BackgroundFill(new Color(0.5, 0.5, 0.5, 0.2), null, null)));
        setTop(titleBox = new HBox(4,
                title = new TextField(),
                editButton = new Button(),
                closeButton = new Button()));
        setBottom(addressBox = new HBox(4,
                address = new Label("0000"),
                functions = new ComboBox<>(observableArrayList(FunctionCode.values())),
                codec = new ComboBox<>())
        );
        setPadding(new Insets(5));
        titleBox.setAlignment(Pos.BASELINE_LEFT);
        titleBox.setPadding(new Insets(4));
        addressBox.setAlignment(Pos.BASELINE_LEFT);
        addressBox.setPadding(new Insets(4));
        title.textProperty().bind(ls(getClass().getSimpleName()));
        editButton.setGraphic(glyphIcon("D_TOOLTIP_EDIT", 16));
        HBox.setHgrow(title, Priority.ALWAYS);
        HBox.setHgrow(codec, Priority.ALWAYS);
        functions.getSelectionModel().select(FunctionCode.ReadHoldingRegisters);
        functions.setConverter(new MaridConverter<>(f -> String.format("%02X", f.getCode())));
        codec.setMaxWidth(Double.MAX_VALUE);
        codec.setConverter(new MaridConverter<>(ModbusCodec::getName));
    }

    @PostConstruct
    private void initCloseButton() {
        closeButton.setGraphic(glyphIcon("D_CLOSE_BOX", 16));
        closeButton.setOnAction(event -> ((ModbusPane) getParent()).getChildren().remove(this));
    }

    @Autowired
    private void initEditButton(@Modbus Stage stage, GenericApplicationContext ctx) {
        editButton.setOnAction(event -> ctx.getBean(getEditor(), this, stage).showAndWait());
    }

    @Autowired
    private void initCodec(CodecManager codecManager) {
        codec.setItems(codecManager.getCodecs(type));
        codec.getSelectionModel().select(0);
    }

    public int getAddress() {
        return Integer.parseInt(address.getText(), 16);
    }

    public FunctionCode getFunctionCode() {
        return functions.getValue();
    }

    public abstract byte[] getData();

    public abstract Class<? extends AbstractDeviceEditor<T, ? extends AbstractDevice<T>>> getEditor();

    public void writeTo(Document document, Element element) {
        element.setAttribute("address", address.getText());
        element.setAttribute("func", functions.getSelectionModel().getSelectedItem().name());
        element.setAttribute("codec", codec.getSelectionModel().getSelectedItem().getName());
    }

    public void loadFrom(Document document, Element element) {
        address.setText(element.getAttribute("address"));
        functions.getItems().stream()
                .filter(f -> f.name().equals(element.getAttribute("func")))
                .forEach(e -> functions.getSelectionModel().select(e));
        codec.getItems().stream()
                .filter(c -> c.getName().equals(element.getAttribute("codec")))
                .forEach(e -> codec.getSelectionModel().select(e));
    }
}
