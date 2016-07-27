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

package org.marid.spring.xml.data;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.stream.Stream;

import static org.marid.spring.xml.MaridBeanDefinitionSaver.SPRING_SCHEMA_PREFIX;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanFile extends AbstractData<BeanFile> {

    public final ObservableList<BeanData> beans = FXCollections.observableArrayList();

    public Stream<BeanLike> allBeans() {
        final Stream.Builder<BeanLike> builder = Stream.builder();
        beans.forEach(builder::add);
        return builder.build();
    }

    @Override
    public void save(Node node, Document document) {
        final Element beans = document.createElement("beans");
        document.appendChild(beans);
        beans.setAttribute("xmlns", SPRING_SCHEMA_PREFIX + "beans");
        beans.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:lang", SPRING_SCHEMA_PREFIX + "lang");
        beans.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:util", SPRING_SCHEMA_PREFIX + "util");
        beans.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:context", SPRING_SCHEMA_PREFIX + "context");

        this.beans.forEach(beanData -> beanData.save(beans, document));
    }

    @Override
    public void load(Node node, Document document) {
        final Element beans = document.getDocumentElement();
        final NodeList nodeList = beans.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            final Node n = nodeList.item(i);
            if (!(n instanceof Element) || n.getNodeName() == null) {
                continue;
            }
            final Element e = (Element) n;
            switch (e.getNodeName()) {
                case "bean":
                    final BeanData beanData = new BeanData();
                    beanData.load(e, document);
                    this.beans.add(beanData);
                    break;
            }
        }
    }
}
