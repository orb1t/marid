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

import org.marid.spring.xml.data.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridBeanDefinitionLoader {

    public static BeanFile load(Path path) throws IOException {
        try (final InputStream inputStream = Files.newInputStream(path)) {
            return load(inputStream);
        }
    }

    public static BeanFile load(InputStream stream) throws IOException {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setCoalescing(true);
        documentBuilderFactory.setNamespaceAware(true);
        try {
            final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            final Document document = documentBuilder.parse(stream);
            return new Loader(document).load();
        } catch (SAXException | ParserConfigurationException x) {
            throw new IOException(x);
        }
    }

    private static class Loader {

        private final Element beans;

        private Loader(Document document) {
            this.beans = document.getDocumentElement();
        }

        private BeanFile load() {
            final BeanFile beanFile = new BeanFile();
            final NodeList nodeList = beans.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                final Node node = nodeList.item(i);
                if (!(node instanceof Element) || node.getNodeName() == null) {
                    continue;
                }
                final Element e = (Element) node;
                switch (e.getNodeName()) {
                    case "bean":
                        final BeanData beanData = new BeanData();
                        fillBeanData(beanData, e);
                        beanData.name.set(e.getAttribute("name"));
                        beanData.type.set(e.getAttribute("class"));
                        beanData.lazyInit.set(e.getAttribute("lazy-init"));
                        beanData.initMethod.set(e.getAttribute("init-method"));
                        beanData.destroyMethod.set(e.getAttribute("destroy-method"));
                        beanData.factoryBean.set(e.getAttribute("factory-bean"));
                        beanData.factoryMethod.set(e.getAttribute("factory-method"));
                        beanFile.beans.add(beanData);
                        break;
                    case "util:constant":
                        final UtilConstant constant = new UtilConstant();
                        constant.id.set(e.getAttribute("id"));
                        constant.staticField.set(e.getAttribute("static-field"));
                        beanFile.constants.add(constant);
                        break;
                }
            }
            return beanFile;
        }

        private void fillBeanData(BeanData beanData, Element bean) {
            final NodeList nodeList = bean.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                final Node node = nodeList.item(i);
                if (!(node instanceof Element) || node.getNodeName() == null) {
                    continue;
                }
                final Element e = (Element) node;
                switch (e.getNodeName()) {
                    case "constructor-arg":
                        final ConstructorArg ca = new ConstructorArg();
                        ca.name.set(e.getAttribute("name"));
                        ca.ref.set(e.getAttribute("ref"));
                        ca.type.set(e.getAttribute("type"));
                        ca.value.set(e.getAttribute("value"));
                        beanData.constructorArgs.add(ca);
                        break;
                    case "property":
                        final Property p = new Property();
                        p.name.set(e.getAttribute("name"));
                        p.ref.set(e.getAttribute("ref"));
                        p.type.set(e.getAttribute("type"));
                        p.value.set(e.getAttribute("value"));
                        beanData.properties.add(p);
                        break;
                }
            }
        }
    }
}
