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

package org.marid.xml;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Iterator;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;

/**
 * @author Dmitry Ovchinnikov
 */
public class NodeListIterator implements Iterator<Node> {

    private final IntFunction<Node> elements;
    private final IntSupplier size;
    int index;

    public NodeListIterator(IntFunction<Node> elements, IntSupplier size) {
        this.elements = elements;
        this.size = size;
    }

    public NodeListIterator(NodeList nodeList) {
        this(nodeList::item, nodeList::getLength);
    }

    @Override
    public boolean hasNext() {
        return index < size.getAsInt();
    }

    @Override
    public Node next() {
        return elements.apply(index++);
    }
}
