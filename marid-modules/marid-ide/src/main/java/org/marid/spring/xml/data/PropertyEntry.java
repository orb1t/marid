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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import static org.apache.commons.lang3.StringUtils.stripToEmpty;
import static org.apache.commons.lang3.StringUtils.stripToNull;

/**
 * @author Dmitry Ovchinnikov.
 */
public class PropertyEntry extends AbstractData<PropertyEntry> {

    public final StringProperty key = new SimpleStringProperty(this, "key");
    public final StringProperty value = new SimpleStringProperty(this, "value");

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(stripToEmpty(key.get()));
        out.writeUTF(stripToEmpty(value.get()));
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        key.set(stripToNull(in.readUTF()));
        value.set(stripToNull(in.readUTF()));
    }

    @Override
    public void save(Node node, Document document) {
        if (key.isNotEmpty().get() && value.isNotEmpty().get()) {
            final Element e = document.createElement("prop");
            node.appendChild(e);
            e.setAttribute("key", key.get());
            e.setTextContent(value.get());
        }
    }

    @Override
    public void load(Node node, Document document) {
        key.set(((Element) node).getAttribute("key"));
        value.set(node.getTextContent());
    }
}
