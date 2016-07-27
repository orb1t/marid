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
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.marid.spring.xml.data.props.Props;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import static org.marid.spring.xml.MaridBeanUtils.setAttr;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class RefValue<T extends RefValue<T>> extends AbstractData<T> {

    public final StringProperty name = new SimpleStringProperty(this, "name");
    public final StringProperty ref = new SimpleStringProperty(this, "ref");
    public final StringProperty value = new SimpleStringProperty(this, "value");
    public final StringProperty type = new SimpleStringProperty(this, "type");

    public final ObjectProperty<Props> props = new SimpleObjectProperty<>(this, "props");

    public boolean isEmpty() {
        return ref.isEmpty().get() && value.isEmpty().get();
    }

    protected abstract String elementName();

    @Override
    public void save(Node node, Document document) {
        if (isEmpty()) {
            return;
        }
        final Element element = document.createElement(elementName());
        node.appendChild(element);
        setAttr(name, element);
        if (ref.isNotEmpty().get()) {
            setAttr(ref, element);
        } else {
            setAttr(value, element);
        }
        setAttr(type, element);
    }

    @Override
    public void load(Node node, Document document) {
        final Element e = (Element) node;
        name.set(e.getAttribute("name"));
        ref.set(e.getAttribute("ref"));
        type.set(e.getAttribute("type"));
        value.set(e.getAttribute("value"));
    }
}
