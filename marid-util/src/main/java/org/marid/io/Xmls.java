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

package org.marid.io;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.newBufferedReader;

/**
 * @author Dmitry Ovchinnikov
 */
public interface Xmls {

    static void write(Consumer<DocumentBuilderFactory> documentBuilderFactoryConfigurer,
                      Consumer<DocumentBuilder> documentBuilderConfigurer,
                      Consumer<Document> documentConfigurer,
                      Consumer<TransformerFactory> transformerFactoryConfigurer,
                      Consumer<Transformer> transformerConfigurer,
                      Result result) {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        documentBuilderFactoryConfigurer.accept(factory);
        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactoryConfigurer.accept(transformerFactory);
        try {
            final DocumentBuilder builder = factory.newDocumentBuilder();
            documentBuilderConfigurer.accept(builder);
            final Document document = builder.newDocument();
            documentConfigurer.accept(document);
            final Transformer transformer = transformerFactory.newTransformer();
            transformerConfigurer.accept(transformer);
            transformer.transform(new DOMSource(document), result);
        } catch (ParserConfigurationException | TransformerException x) {
            throw new IllegalStateException(x);
        }
    }

    static <T> T read(Consumer<DocumentBuilderFactory> documentBuilderFactoryConfigurer,
                     Consumer<DocumentBuilder> documentBuilderConfigurer,
                     Function<Document, T> documentReader,
                     InputSource source) {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        documentBuilderFactoryConfigurer.accept(factory);
        try {
            final DocumentBuilder builder = factory.newDocumentBuilder();
            documentBuilderConfigurer.accept(builder);
            final Document document = builder.parse(source);
            return documentReader.apply(document);
        } catch (ParserConfigurationException | SAXException x) {
            throw new IllegalStateException(x);
        } catch (IOException x) {
            throw new UncheckedIOException(x);
        }
    }

    static void writeFormatted(Consumer<Document> documentConsumer, Path file) {
        try (final BufferedWriter writer = Files.newBufferedWriter(file, UTF_8)) {
            write(f -> {}, b -> {}, documentConsumer, f -> {}, t -> {
                t.setOutputProperty(OutputKeys.INDENT, "yes");
                t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            }, new StreamResult(writer));
        } catch (IOException x) {
            throw new UncheckedIOException(x);
        }
    }

    static <T> T read(Function<Document, T> documentReader, Path file) {
        try (final BufferedReader reader = newBufferedReader(file, UTF_8)) {
            return read(documentReader, reader);
        } catch (IOException x) {
            throw new UncheckedIOException(x);
        }
    }

    static <T> T read(Path file, Function<Element, T> elementReader) {
        return read(d -> elementReader.apply(d.getDocumentElement()), file);
    }

    static <T> T read(Function<Document, T> documentReader, Reader reader) {
        return read(f -> {}, b -> {}, documentReader, new InputSource(reader));
    }

    static <T> T read(Reader reader, Function<Element, T> elementReader) {
        return read(d -> elementReader.apply(d.getDocumentElement()), reader);
    }

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

    static Optional<String> attribute(Element element, String name) {
        return element.hasAttribute(name)
                ? Optional.of(element.getAttribute(name))
                : Optional.empty();
    }

    static Optional<String> content(Element element) {
        return element.hasChildNodes()
                ? Optional.of(element.getTextContent())
                : Optional.empty();
    }
}
