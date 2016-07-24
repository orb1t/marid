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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.stream.Stream;

import static org.marid.spring.xml.MaridBeanDefinitionSaver.SPRING_SCHEMA_PREFIX;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanFile extends AbstractData<BeanFile> {

    public final ObservableList<BeanData> beans = FXCollections.observableArrayList();
    public final ObservableList<UtilProperties> properties = FXCollections.observableArrayList();
    public final ObservableList<UtilConstant> constants = FXCollections.observableArrayList();

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(beans.size());
        for (final BeanData beanData : beans) {
            out.writeObject(beanData);
        }

        out.writeInt(properties.size());
        for (final UtilProperties prop : properties) {
            out.writeObject(prop);
        }

        out.writeInt(constants.size());
        for (final UtilConstant constant : constants) {
            out.writeObject(constant);
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        final int beanCount = in.readInt();
        for (int i = 0; i < beanCount; i++) {
            beans.add((BeanData) in.readObject());
        }

        final int propCount = in.readInt();
        for (int i = 0; i < propCount; i++) {
            properties.add((UtilProperties) in.readObject());
        }

        final int constCount = in.readInt();
        for (int i = 0; i < constCount; i++) {
            constants.add((UtilConstant) in.readObject());
        }
    }

    public Stream<BeanLike> allBeans() {
        final Stream.Builder<BeanLike> builder = Stream.builder();
        beans.forEach(builder::add);
        properties.forEach(builder::add);
        constants.forEach(builder::add);
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
        this.constants.forEach(constants -> constants.save(beans, document));
        this.properties.forEach(properties -> properties.save(beans, document));
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
                case "util:constant":
                    final UtilConstant constant = new UtilConstant();
                    constant.load(e, document);
                    this.constants.add(constant);
                    break;
                case "util:properties":
                    final UtilProperties properties = new UtilProperties();
                    properties.load(e, document);
                    this.properties.add(properties);
                    break;
            }
        }
    }
}
