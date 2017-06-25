package org.marid.misc;

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
