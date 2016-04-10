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

package org.marid.ide.beaneditor.data;

import javafx.beans.property.StringProperty;
import javafx.scene.control.TreeItem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.nio.file.Path;

/**
 * @author Dmitry Ovchinnikov
 */
public class Saver {

    private static final String SPRING_SCHEMA_PREFIX = "http://www.springframework.org/schema/";

    public void save(TreeItem<Object> it) throws Exception {
        if (it.getValue() instanceof Path) {
            if (it.getChildren().stream().noneMatch(e -> e.getValue() instanceof Path)) {
                final Path path = (Path) it.getValue();
                save(path, it);
                return;
            }
        }
        for (final TreeItem<Object> item : it.getChildren()) {
            save(item);
        }
    }

    private void save(Path path, TreeItem<Object> pathItem) throws Exception {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        final Document document = documentBuilder.newDocument();
        final Element beansElement = document.createElement("beans");
        beansElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:context", xsdPath("context"));
        beansElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:util", xsdPath("util"));
        beansElement.setAttribute("xmlns", xsdPath("beans"));
        document.appendChild(beansElement);
        beansElement.appendChild(document.createElementNS(xsdPath("context"), "context:annotation-config"));
        save(document, pathItem, beansElement);
        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        final Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.transform(new DOMSource(document), new StreamResult(path.toFile()));
    }

    private void save(Document document, TreeItem<Object> pathItem, Element beansElement) {
        pathItem.getChildren().filtered(e -> e.getValue() instanceof BeanData).forEach(beanItem -> {
            final BeanData beanData = (BeanData) beanItem.getValue();
            final Element beanElement = document.createElement("bean");
            beansElement.appendChild(beanElement);
            attr(beanElement, beanData.name);
            attr(beanElement, beanData.type);
            attr(beanElement, beanData.initMethod);
            attr(beanElement, beanData.destroyMethod);
            attr(beanElement, beanData.factoryBean);
            attr(beanElement, beanData.factoryMethod);
            attr(beanElement, beanData.lazyInit);
            beanItem.getChildren().filtered(e -> e.getValue() instanceof ConstructorArg).forEach(item -> {
                final ConstructorArg d = (ConstructorArg) item.getValue();
                if (d.ref.isNotEmpty().get() || d.value.isNotEmpty().get()) {
                    final Element element = document.createElement("constructor-arg");
                    beanElement.appendChild(element);
                    attr(element, d.name);
                    attr(element, d.ref);
                    attr(element, d.value);
                }
            });
            beanItem.getChildren().filtered(e -> e.getValue() instanceof Property).forEach(item -> {
                final Property d = (Property) item.getValue();
                if (d.ref.isNotEmpty().get() || d.value.isNotEmpty().get()) {
                    final Element element = document.createElement("property");
                    beanElement.appendChild(element);
                    attr(element, d.name);
                    attr(element, d.ref);
                    attr(element, d.value);
                }
            });
        });
    }

    private static String xsdPath(String schema) {
        return SPRING_SCHEMA_PREFIX + schema;
    }

    private static void attr(Element element, StringProperty property) {
        if (property.isNotEmpty().get()) {
            element.setAttribute(property.getName(), property.get());
        }
    }
}
