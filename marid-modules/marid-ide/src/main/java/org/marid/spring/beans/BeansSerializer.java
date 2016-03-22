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

package org.marid.spring.beans;

import org.apache.commons.lang3.StringUtils;
import org.marid.misc.Builder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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
        return deserialize(new InputSource(inputStream));
    }

    public static Beans deserialize(File file) throws IOException {
        return deserialize(new InputSource(file.toURI().toASCIIString()));
    }

    public static Beans deserialize(InputSource inputSource) throws IOException {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document document = builder.parse(inputSource);
            final Element docElement = document.getDocumentElement();
            final Beans beans = new Beans();
            if (docElement == null) {
                return beans;
            }
            for (int bi = 0; bi < docElement.getChildNodes().getLength(); bi++) {
                final Node beanNode = docElement.getChildNodes().item(bi);
                if (beanNode.getNodeName() == null) {
                    continue;
                }
                switch (beanNode.getNodeName()) {
                    case "bean":
                        final Bean bean = new Bean();
                        bean.name = attr(beanNode, "name");
                        bean.beanClass = attr(beanNode, "class");
                        bean.initMethod = attr(beanNode, "init-method");
                        bean.destroyMethod = attr(beanNode, "destroy-method");
                        bean.factoryBean = attr(beanNode, "factory-bean");
                        bean.factoryMethod = attr(beanNode, "factory-method");
                        for (int ci = 0; ci < beanNode.getChildNodes().getLength(); ci++) {
                            final Node childNode = beanNode.getChildNodes().item(ci);
                            if (childNode.getNodeName() == null) {
                                continue;
                            }
                            switch (childNode.getNodeName()) {
                                case "constructor-arg":
                                    bean.constructorArgs.add(Builder.build(new ConstructorArg(), arg -> {
                                        arg.name = attr(childNode, "name");
                                        arg.type = attr(childNode, "type");
                                        arg.ref = attr(childNode, "ref");
                                        arg.value = attr(childNode, "value");
                                    }));
                                    break;
                                case "property":
                                    bean.propertyArgs.add(Builder.build(new PropertyArg(), arg -> {
                                        arg.name = attr(childNode, "name");
                                        arg.ref = attr(childNode, "ref");
                                        arg.value = attr(childNode, "value");
                                    }));
                                    break;
                            }
                        }
                        beans.beans.add(bean);
                        break;
                }
            }
            return beans;
        } catch (ParserConfigurationException | SAXException x) {
            throw new IOException(x);
        }
    }

    public static void serialize(Beans beans, Result result) throws IOException {
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
            beansElement.appendChild(document.createElementNS(xsdPath("context"), "context:annotation-config"));
            for (final Bean bean : beans.beans) {
                final Element beanElement = document.createElement("bean");
                attr(beanElement, "class", bean.beanClass);
                attr(beanElement, "name", bean.name);
                attr(beanElement, "init-method", bean.initMethod);
                attr(beanElement, "destroy-method", bean.destroyMethod);
                attr(beanElement, "factory-bean", bean.factoryBean);
                attr(beanElement, "factory-method", bean.factoryMethod);
                for (final ConstructorArg arg : bean.constructorArgs) {
                    final Element ce = document.createElement("constructor-arg");
                    attr(ce, "name", arg.name);
                    attr(ce, "ref", arg.ref);
                    attr(ce, "value", arg.value);
                    attr(ce, "type", arg.type);
                    beanElement.appendChild(ce);
                }
                for (final PropertyArg arg : bean.propertyArgs) {
                    final Element pe = document.createElement("property");
                    attr(pe, "name", arg.name);
                    attr(pe, "ref", arg.ref);
                    attr(pe, "value", arg.value);
                    beanElement.appendChild(pe);
                }
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

    private static String attr(Node node, String name) {
        final Node attrNode = node.getAttributes().getNamedItem(name);
        return attrNode == null ? null : attrNode.getNodeValue();
    }

    private static void attr(Element node, String name, String value) {
        if (StringUtils.isNotBlank(value)) {
            node.setAttribute(name, value);
        }
    }
}
