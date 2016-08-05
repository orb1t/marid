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

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import static org.marid.spring.xml.MaridBeanUtils.setAttr;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class RefValue<T extends RefValue<T>> extends ValueHolder<T> {

    public final StringProperty name = new SimpleStringProperty(this, "name");
    public final StringProperty ref = new SimpleStringProperty(this, "ref");
    public final StringProperty value = new SimpleStringProperty(this, "value");
    public final StringProperty type = new SimpleStringProperty(this, "type");

    public boolean isEmpty() {
        return ref.isEmpty().get() && value.isEmpty().get() && super.isEmpty();
    }

    @Override
    protected void doSave(Element element, Node node, Document document) {
        setAttr(name, element);
        if (ref.isNotEmpty().get()) {
            setAttr(ref, element);
        } else {
            setAttr(value, element);
        }
        setAttr(type, element);
    }

    @Override
    protected void doLoad(Element element, Node node, Document document) {
        name.set(element.getAttribute("name"));
        ref.set(element.getAttribute("ref"));
        type.set(element.getAttribute("type"));
        value.set(element.getAttribute("value"));
    }
}
