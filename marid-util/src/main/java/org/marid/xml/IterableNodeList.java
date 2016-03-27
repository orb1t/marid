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

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Dmitry Ovchinnikov
 */
public class IterableNodeList implements NodeList, Iterable<Node> {

    private final NodeList nodeList;

    public IterableNodeList(@Nonnull NodeList nodeList) {
        this.nodeList = nodeList;
    }

    @Override
    public Node item(int index) {
        return nodeList.item(index);
    }

    @Override
    public int getLength() {
        return nodeList.getLength();
    }

    @Override
    public int hashCode() {
        return nodeList.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof IterableNodeList && ((IterableNodeList) obj).nodeList.equals(nodeList);
    }

    @Override
    public String toString() {
        return nodeList.toString();
    }

    @Override
    public Iterator<Node> iterator() {
        return new NodeListIterator(nodeList);
    }

    public Stream<Node> stream() {
        return StreamSupport.stream(spliterator(), false);
    }
}
