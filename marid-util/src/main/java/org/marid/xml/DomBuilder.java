/*-
 * #%L
 * marid-util
 * %%
 * Copyright (C) 2012 - 2018 MARID software development group
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

package org.marid.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.function.*;
import java.util.stream.Stream;

import static org.marid.misc.StringUtils.stringOrNull;

public class DomBuilder {

  private final Element element;

  public DomBuilder(Element element) {
    this.element = element;
  }

  public Element getElement() {
    return element;
  }

  public Document getDocument() {
    return element.getOwnerDocument();
  }

  public Node getNodeToTransform() {
    return element;
  }

  public DomBuilder text(String text) {
    element.setTextContent(text);
    return this;
  }

  public DomBuilder text(Supplier<String> text) {
    element.setTextContent(text.get());
    return this;
  }

  public DomBuilder attrs(Map<String, ?> attrs) {
    attrs.forEach((k, v) -> element.setAttribute(k, stringOrNull(v)));
    return this;
  }

  @SafeVarargs
  public final DomBuilder child(String tag, Map<String, ?> attrs, Consumer<DomBuilder>... childConfigurers) {
    final Element child = getDocument().createElement(tag);
    element.appendChild(child);

    final DomBuilder domBuilder = new DomBuilder(child).attrs(attrs);
    for (final Consumer<DomBuilder> childConfigurer : childConfigurers) {
      childConfigurer.accept(domBuilder);
    }
    return this;
  }

  @SafeVarargs
  public final DomBuilder child(String tag, Consumer<DomBuilder>... childConfigurers) {
    final Element child = getDocument().createElement(tag);
    element.appendChild(child);

    final DomBuilder domBuilder = new DomBuilder(child);
    for (final Consumer<DomBuilder> childConfigurer : childConfigurers) {
      childConfigurer.accept(domBuilder);
    }
    return this;
  }

  @SafeVarargs
  public final <E> DomBuilder children(String tag, Stream<E> stream, BiConsumer<DomBuilder, E>... configurers) {
    stream.forEach(e -> {
      final Element child = getDocument().createElement(tag);
      element.appendChild(child);

      final DomBuilder domBuilder = new DomBuilder(child);
      for (final BiConsumer<DomBuilder, E> configurer : configurers) {
        configurer.accept(domBuilder, e);
      }
    });
    return this;
  }

  @SafeVarargs
  public final <E> DomBuilder with(E e, BiConsumer<DomBuilder, E>... configurers) {
    for (final BiConsumer<DomBuilder, E> configurer : configurers) {
      configurer.accept(this, e);
    }
    return this;
  }

  @SafeVarargs
  public final <E> DomBuilder with(E e, Predicate<E> filter, BiConsumer<DomBuilder, E>... configurers) {
    if (filter.test(e)) {
      return with(e, configurers);
    } else {
      return this;
    }
  }

  @SafeVarargs
  public final <E> DomBuilder with(Supplier<E> supplier, BiConsumer<DomBuilder, E>... configurers) {
    final E e = supplier.get();
    for (final BiConsumer<DomBuilder, E> configurer : configurers) {
      configurer.accept(this, e);
    }
    return this;
  }

  @SafeVarargs
  public final <E> DomBuilder with(Supplier<E> supplier, Predicate<E> filter, BiConsumer<DomBuilder, E>... configurers) {
    final E e = supplier.get();
    if (filter.test(e)) {
      return with(e, configurers);
    } else {
      return this;
    }
  }

  @SafeVarargs
  public final DomBuilder when(BooleanSupplier conditionSupplier, Consumer<DomBuilder>... configurers) {
    if (conditionSupplier.getAsBoolean()) {
      for (final Consumer<DomBuilder> configurer : configurers) {
        configurer.accept(this);
      }
    }
    return this;
  }

  @SafeVarargs
  public final DomBuilder when(boolean condition, Consumer<DomBuilder>... configurers) {
    if (condition) {
      for (final Consumer<DomBuilder> configurer : configurers) {
        configurer.accept(this);
      }
    }
    return this;
  }

  public DomBuilder write(Result result) throws IOException {
    final TransformerFactory transformerFactory = TransformerFactory.newDefaultInstance();
    transformerFactory.setAttribute("indent-number", 2);

    try {
      final Transformer transformer = transformerFactory.newTransformer();

      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty(OutputKeys.METHOD, "html");
      transformer.setOutputProperty(OutputKeys.VERSION, "5.0");
      transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "about:legacy-compat");

      transformer.transform(new DOMSource(getNodeToTransform()), result);

      return this;
    } catch (TransformerException x) {
      if (x.getCause() instanceof IOException) {
        throw (IOException) x.getCause();
      } else {
        throw new IOException(x);
      }
    }
  }

  public DomBuilder appendTo(Result result) {
    try {
      return write(result);
    } catch (IOException exception) {
      throw new UncheckedIOException(exception);
    }
  }
}
