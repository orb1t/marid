package org.marid.dependant.modbus;

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

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import org.marid.dependant.modbus.devices.AbstractDevice;
import org.marid.dependant.modbus.repo.ModbusConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

import static java.util.Optional.of;
import static java.util.logging.Level.WARNING;
import static org.marid.logging.Log.log;
import static org.marid.misc.Iterables.nodes;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
@Component
public class ModbusPane extends FlowPane {

    private final GenericApplicationContext factory;
    private final ModbusConfig config;

    @Autowired
    public ModbusPane(GenericApplicationContext factory, ModbusConfig config) {
        super(10, 10);
        this.factory = factory;
        this.config = config;
        setPadding(new Insets(10));
    }

    public void save(File file) {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        try {
            final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            final Document document = documentBuilder.newDocument();
            final Element root = document.createElement("devices");
            root.setAttribute("host", config.host.get());
            root.setAttribute("port", Integer.toString(config.port.get()));
            document.appendChild(root);
            getChildren().stream()
                    .filter(AbstractDevice.class::isInstance)
                    .map(AbstractDevice.class::cast)
                    .forEach(d -> {
                        final String name = d.getProperties().get("name").toString();
                        final Element element = document.createElement("device");
                        element.setAttribute("name", name);
                        d.writeTo(document, element);
                        document.getDocumentElement().appendChild(element);
                    });
            final Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(new DOMSource(document), new StreamResult(file));
            initTitle(file);
        } catch (ParserConfigurationException | TransformerException x) {
            throw new IllegalStateException(x);
        }
    }

    public void load(File file) {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            final Document document = documentBuilder.parse(file);
            final Element root = document.getDocumentElement();
            for (final Element element : nodes(root, Element.class, e -> "device".equals(e.getTagName()))) {
                final AbstractDevice<?> device = factory.getBean(element.getAttribute("name"), AbstractDevice.class);
                device.loadFrom(document, element);
                getChildren().add(device);
            }
            of(root.getAttribute("host")).filter(s -> !s.isEmpty()).ifPresent(config.host::set);
            of(root.getAttribute("port")).filter(s -> !s.isEmpty()).map(Integer::valueOf).ifPresent(config.port::set);
            initTitle(file);
        } catch (Exception x) {
            log(WARNING, "Unable to load {0}", x, file);
        }
    }

    public <T> void add(AbstractDevice<T> device) {
        getChildren().add(device);
    }

    private void initTitle(File file) {
        if (getScene() == null) {
            return;
        }
        if (getScene().getWindow() == null) {
            return;
        }
        final Stage stage = (Stage) getScene().getWindow();
        stage.titleProperty().bind(Bindings.createObjectBinding(() -> String.format("MODBUS: %s", file.getName())));
    }
}
