package org.marid.misc;

/*-
 * #%L
 * marid-util
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Spliterators;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
public interface Iterables {

    static <E> Stream<E> stream(Class<E> type, Stream<?> stream) {
        return stream.filter(type::isInstance).map(type::cast);
    }

    static <E extends Node> Iterable<E> nodes(Node node, Class<E> type, Predicate<E> filter) {
        return () -> Spliterators.iterator(nodes(node, type).filter(filter).spliterator());
    }

    static <E extends Node> Stream<E> nodes(Node node, Class<E> type) {
        final NodeList children = node.getChildNodes();
        return IntStream.range(0, children.getLength())
                .mapToObj(children::item)
                .filter(type::isInstance)
                .map(type::cast);
    }
}
