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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridBeanDefinitionSaver {

    public static void write(Path path, BeanFile beanFile) throws IOException {
        write(new StreamResult(path.toFile()), beanFile);
    }

    public static void write(Result result, BeanFile beanFile) throws IOException {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        try {
            final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            final Document document = documentBuilder.newDocument();
            final Element beans = (Element) document.appendChild(document.createElement("beans"));
            beans.setAttribute("xmlns", "http://www.springframework.org/schema/beans");
            beanFile.writeTo(document, beans);
            final Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(new DOMSource(document), result);
        } catch (ParserConfigurationException | TransformerException x) {
            throw new IOException(x);
        }
    }
}
