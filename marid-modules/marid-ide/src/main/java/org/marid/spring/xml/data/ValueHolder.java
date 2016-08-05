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

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.marid.spring.xml.data.list.DList;
import org.marid.spring.xml.data.props.DProps;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.stream.Stream;

/**
 * @author Dmitry Ovchinnikov.
 */
public abstract class ValueHolder<T extends ValueHolder<T>> extends AbstractData<T> {

    public final ObjectProperty<DProps> props = new SimpleObjectProperty<>(this, "props");
    public final ObjectProperty<DList> list = new SimpleObjectProperty<>(this, "list");

    @Override
    public void save(Node node, Document document) {
        if (isEmpty()) {
            return;
        }
        final Element element = document.createElement(elementName());
        node.appendChild(element);

        doSave(element, node, document);

        if (props.isNotNull().get()) {
            props.get().save(element, document);
        }
        if (list.isNotNull().get()) {
            list.get().save(element, document);
        }
    }

    protected abstract void doSave(Element element, Node node, Document document);

    @Override
    public void load(Node node, Document document) {
        final Element element = (Element) node;

        doLoad(element, node, document);

        final NodeList nodeList = element.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            if (nodeList.item(i) instanceof Element) {
                final Element e = (Element) nodeList.item(i);
                switch (e.getNodeName()) {
                    case "props":
                        final DProps props = new DProps();
                        this.props.set(props);
                        props.load(e, document);
                        break;
                    case "list":
                        final DList list = new DList();
                        this.list.set(list);
                        list.load(e, document);
                        break;
                }
            }
        }
    }

    protected abstract void doLoad(Element element, Node node, Document document);

    public boolean isEmpty() {
        return Stream.of(props, list)
                .allMatch(e -> e.isNull().get());
    }

    protected abstract String elementName();
}
