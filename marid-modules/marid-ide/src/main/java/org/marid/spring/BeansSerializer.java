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

package org.marid.spring;

import org.marid.spring.xml.Bean;
import org.marid.spring.xml.Beans;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeansSerializer {

    public static final String SPRING_SCHEMA_PREFIX = "http://www.springframework.org/schema/";

    private static String xsdLocation(String schema) {
        return String.format("%s%s/spring-%s.xsd", SPRING_SCHEMA_PREFIX, schema, schema);
    }

    private static String xsdPath(String schema) {
        return SPRING_SCHEMA_PREFIX + schema;
    }

    public static void serialize(Beans beans, OutputStream outputStream) throws IOException {
        serialize(beans, new StreamResult(outputStream));
    }

    public static void serialize(Beans beans, File file) throws IOException {
        serialize(beans, new StreamResult(file));
    }

    public static Beans deserialize(InputStream inputStream) throws IOException {
        return null;
    }

    public static Beans deserialize(File file) throws IOException {
        return null;
    }

    private static void serialize(Beans beans, Result result) throws IOException {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document document = builder.newDocument();
            final Element beansElement = document.createElement("beans");
            beansElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:context", xsdPath("context"));
            beansElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:util", xsdPath("util"));
            beansElement.setAttribute("xmlns", xsdPath("beans"));
            document.appendChild(beansElement);
            for (final Bean bean : beans.beans) {
                final Element beanElement = document.createElement("bean");
                beanElement.setAttribute("class", bean.beanClass);
                beanElement.setAttribute("name", bean.name);
                beansElement.appendChild(beanElement);
            }
            final TransformerFactory transformerFactory = TransformerFactory.newInstance();
            final Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(new DOMSource(document), result);
        } catch (ParserConfigurationException | TransformerException x) {
            throw new IOException(x);
        }
    }
}
