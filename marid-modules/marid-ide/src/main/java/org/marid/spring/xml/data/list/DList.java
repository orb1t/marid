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

package org.marid.spring.xml.data.list;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.marid.spring.xml.MaridBeanUtils;
import org.marid.spring.xml.data.AbstractData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Dmitry Ovchinnikov.
 */
public class DList extends AbstractData<DList> {

    public final StringProperty valueType = new SimpleStringProperty(this, "value-type", Object.class.getName());
    public final ObservableList<DListEntry> entries = FXCollections.observableArrayList();

    @Override
    public void save(Node node, Document document) {
        if (entries.isEmpty()) {
            return;
        }
        final Element element = document.createElement("list");
        node.appendChild(element);

        MaridBeanUtils.setAttr(valueType, element);

        entries.forEach(e -> e.save(element, document));
    }

    @Override
    public void load(Node node, Document document) {
        final Element element = (Element) node;

        MaridBeanUtils.setProperty(valueType, element);

        final NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            final Node n = nodeList.item(i);
            if (n instanceof Element) {
                final Element e = (Element) n;
                switch (e.getNodeName()) {
                    case "value":
                        final DListEntry entry = new DListEntry();
                        entries.add(entry);
                        entry.load(e, document);
                        break;
                }
            }
        }
    }
}
