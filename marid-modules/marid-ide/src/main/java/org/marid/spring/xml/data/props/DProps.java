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

package org.marid.spring.xml.data.props;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.marid.spring.xml.data.AbstractData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static org.marid.spring.xml.MaridBeanUtils.setAttr;
import static org.marid.spring.xml.MaridBeanUtils.setProperty;

/**
 * @author Dmitry Ovchinnikov.
 */
public class DProps extends AbstractData<DProps> {

    public final StringProperty valueType = new SimpleStringProperty(this, "value-type", String.class.getName());
    public final ObservableList<DPropEntry> entries = FXCollections.observableArrayList();

    @Override
    public void save(Node node, Document document) {
        if (entries.isEmpty()) {
            return;
        }
        final Element element = document.createElement("props");
        node.appendChild(element);

        setAttr(valueType, element);

        entries.forEach(entry -> entry.save(element, document));
    }

    @Override
    public void load(Node node, Document document) {
        final Element element = (Element) node;

        setProperty(valueType, element);

        final NodeList children = element.getElementsByTagName("prop");
        for (int i = 0; i < children.getLength(); i++) {
            final Element e = (Element) children.item(i);
            final DPropEntry entry = new DPropEntry();
            entry.load(e, document);
            entries.add(entry);
        }
    }
}
