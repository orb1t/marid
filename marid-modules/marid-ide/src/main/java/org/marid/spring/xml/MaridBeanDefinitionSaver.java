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

package org.marid.spring.xml;

import javafx.beans.property.StringProperty;
import org.marid.spring.xml.data.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridBeanDefinitionSaver {

    private static final String SPRING_SCHEMA_PREFIX = "http://www.springframework.org/schema/";

    public static void write(Path path, BeanFile beanFile) throws IOException {
        try (final OutputStream outputStream = Files.newOutputStream(path)) {
            write(outputStream, beanFile);
        }
    }

    public static void write(OutputStream outputStream, BeanFile beanFile) throws IOException {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        try {
            final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            final Document document = documentBuilder.newDocument();
            final TransformerFactory transformerFactory = TransformerFactory.newInstance();
            final Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            new Saver(document, beanFile).save();
            transformer.transform(new DOMSource(document), new StreamResult(outputStream));
        } catch (ParserConfigurationException | TransformerException x) {
            throw new IOException(x);
        }
    }

    private static class Saver {

        private final Document document;
        private final BeanFile beanFile;
        private final Element beans;

        private Saver(Document document, BeanFile beanFile) {
            this.document = document;
            this.beanFile = beanFile;
            document.appendChild(beans = document.createElement("beans"));
            beans.setAttribute("xmlns", SPRING_SCHEMA_PREFIX + "beans");
            beans.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:lang", SPRING_SCHEMA_PREFIX + "lang");
            beans.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:util", SPRING_SCHEMA_PREFIX + "util");
            beans.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:context", SPRING_SCHEMA_PREFIX + "context");
        }

        private void save() {
            for (final BeanData beanData : beanFile.beans) {
                final Element beanElement = document.createElement("bean");
                beans.appendChild(beanElement);
                setAttr(beanData.name, beanElement);
                setAttr(beanData.destroyMethod, beanElement);
                setAttr(beanData.initMethod, beanElement);
                setAttr(beanData.factoryBean, beanElement);
                setAttr(beanData.factoryMethod, beanElement);
                setAttr(beanData.type, beanElement);
                setAttr(beanData.lazyInit, beanElement);

                for (final ConstructorArg constructorArg : beanData.constructorArgs) {
                    if (constructorArg.isEmpty()) {
                        continue;
                    }
                    final Element element = document.createElement("constructor-arg");
                    beanElement.appendChild(element);
                    setAttr(constructorArg.name, element);
                    if (constructorArg.ref.isNotEmpty().get()) {
                        setAttr(constructorArg.ref, element);
                    } else {
                        setAttr(constructorArg.value, element);
                    }
                    setAttr(constructorArg.type, element);
                }

                for (final Property property : beanData.properties) {
                    if (property.isEmpty()) {
                        continue;
                    }
                    final Element element = document.createElement("property");
                    beanElement.appendChild(element);
                    setAttr(property.name, element);
                    if (property.ref.isNotEmpty().get()) {
                        setAttr(property.ref, element);
                    } else {
                        setAttr(property.value, element);
                    }
                    setAttr(property.type, element);
                }
            }

            for (final UtilConstant constant : beanFile.constants) {
                final Element beanElement = document.createElementNS(SPRING_SCHEMA_PREFIX + "util", "util:constant");
                beans.appendChild(beanElement);
                setAttr(constant.id, beanElement);
                setAttr(constant.staticField, beanElement);
            }

            for (final UtilProperties properties : beanFile.properties) {
                final Element element = document.createElementNS(SPRING_SCHEMA_PREFIX + "util", "util:constant");
                beans.appendChild(element);
                setAttr(properties.id, element);
                setAttr(properties.ignoreResourceNotFound, element);
                setAttr(properties.localOverride, element);
                setAttr(properties.valueType, element);
                setAttr(properties.location, element);

                properties.entries.forEach((k, v) -> {
                    final Element e = document.createElement("prop");
                    element.appendChild(e);
                    e.setAttribute("key", k);
                    e.setTextContent(v);
                });
            }
        }

        private void setAttr(StringProperty property, Element element) {
            if (property.isNotEmpty().get()) {
                element.setAttribute(property.getName(), property.get());
            }
        }
    }
}
