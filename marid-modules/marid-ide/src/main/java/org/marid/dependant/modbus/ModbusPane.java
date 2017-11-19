/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.marid.dependant.modbus;

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
import static org.marid.io.Xmls.nodes;

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
