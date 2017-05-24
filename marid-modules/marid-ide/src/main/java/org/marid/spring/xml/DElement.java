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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class DElement extends AbstractData {

    public abstract boolean isEmpty();

    public static DElement read(Document document, Element element) {
        final String tag = element.getTagName();
        if (tag == null) {
            return null;
        } else {
            switch (tag) {
                case "array":
                    final DArray array = new DArray();
                    array.loadFrom(document, element);
                    return array;
                case "list":
                    final DList list = new DList();
                    list.loadFrom(document, element);
                    return list;
                case "map":
                    final DMap map = new DMap();
                    map.loadFrom(document, element);
                    return map;
                case "ref":
                    final DRef ref = new DRef();
                    ref.loadFrom(document, element);
                    return ref;
                case "props":
                    final DProps props = new DProps();
                    props.loadFrom(document, element);
                    return props;
                case "value":
                    final DValue value = new DValue();
                    value.loadFrom(document, element);
                    return value;
                case "bean":
                    final BeanData beanData = new BeanData();
                    beanData.loadFrom(document, element);
                    return beanData;
                default:
                    return null;
            }
        }
    }

    public static void write(Document document, Element parent, DElement element) {
        final Element child;
        if (element instanceof DArray) {
             child = document.createElement("array");
        } else if (element instanceof DList) {
            child = document.createElement("list");
        } else if (element instanceof DMap) {
            child = document.createElement("map");
        } else if (element instanceof DRef) {
            child = document.createElement("ref");
        } else if (element instanceof DProps) {
            child = document.createElement("props");
        } else if (element instanceof DValue) {
            child = document.createElement("value");
        } else if (element instanceof BeanData) {
            child = document.createElement("bean");
        } else {
            return;
        }
        parent.appendChild(child);
        element.writeTo(document, child);
    }
}
