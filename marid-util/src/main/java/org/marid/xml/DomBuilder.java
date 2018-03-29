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

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.Map;
import java.util.function.*;
import java.util.stream.Stream;

import static org.marid.misc.StringUtils.stringOrNull;

public class DomBuilder {

  final Element element;

  public DomBuilder(Element element) {
    this.element = element;
  }

  Document getDocument() {
    return element.getOwnerDocument();
  }

  public Node getNodeToTransform() {
    return element;
  }

  public DomBuilder t(String text) {
    element.setTextContent(text);
    return this;
  }

  public DomBuilder t(String text, Object... args) {
    return t(String.format(text, args));
  }

  public DomBuilder kv(String attr, Object value) {
    element.setAttribute(attr, stringOrNull(value));
    return this;
  }

  public DomBuilder kv(Map<String, ?> attrs) {
    attrs.forEach(this::kv);
    return this;
  }

  public DomBuilder c(String commentText) {
    element.appendChild(getDocument().createComment(commentText));
    return this;
  }

  public DomBuilder c(String commentText, Object... args) {
    return c(String.format(commentText, args));
  }

  public DomBuilder bc(String commentText) {
    element.getParentNode().insertBefore(getDocument().createComment(commentText), element);
    return this;
  }

  public DomBuilder bc(String commentText, Object... args) {
    return bc(String.format(commentText, args));
  }

  public DomBuilder ac(String commentText) {
    element.getParentNode().appendChild(getDocument().createComment(commentText));
    return this;
  }

  public DomBuilder ac(String commentText, Object... args) {
    return ac(String.format(commentText, args));
  }

  public DomBuilder text(String text) {
    element.appendChild(getDocument().createTextNode(text));
    return this;
  }

  public DomBuilder text(String text, Object... args) {
    return text(String.format(text, args));
  }

  @SafeVarargs
  public final DomBuilder e(String tag, String text, Map<String, ?> attrs, Consumer<DomBuilder>... childConfigurers) {
    return e(tag, attrs, e -> {
      e.t(text);
      for (final Consumer<DomBuilder> childConfigurer : childConfigurers) {
        childConfigurer.accept(e);
      }
    });
  }

  @SafeVarargs
  public final DomBuilder e(String tag, String text, Consumer<DomBuilder>... childConfigurers) {
    return e(tag, Collections.emptyMap(), e -> {
      e.t(text);
      for (final Consumer<DomBuilder> childConfigurer : childConfigurers) {
        childConfigurer.accept(e);
      }
    });
  }

  @SafeVarargs
  public final DomBuilder e(String tag, Map<String, ?> attrs, Consumer<DomBuilder>... childConfigurers) {
    final Element child = getDocument().createElement(tag);
    element.appendChild(child);

    final DomBuilder domBuilder = new DomBuilder(child).kv(attrs);
    for (final Consumer<DomBuilder> childConfigurer : childConfigurers) {
      childConfigurer.accept(domBuilder);
    }
    return this;
  }

  @SafeVarargs
  public final DomBuilder e(String tag, Consumer<DomBuilder>... childConfigurers) {
    final Element child = getDocument().createElement(tag);
    element.appendChild(child);

    final DomBuilder domBuilder = new DomBuilder(child);
    for (final Consumer<DomBuilder> childConfigurer : childConfigurers) {
      childConfigurer.accept(domBuilder);
    }
    return this;
  }

  @SafeVarargs
  public final <E> DomBuilder es(String tag, Stream<E> stream, BiConsumer<E, DomBuilder>... configurers) {
    stream.forEach(e -> {
      final Element child = getDocument().createElement(tag);
      element.appendChild(child);

      final DomBuilder domBuilder = new DomBuilder(child);
      for (final BiConsumer<E, DomBuilder> configurer : configurers) {
        configurer.accept(e, domBuilder);
      }
    });
    return this;
  }

  @SafeVarargs
  public final <E> DomBuilder es(String tag, Iterable<E> iterable, BiConsumer<E, DomBuilder>... configurers) {
    for (final E e : iterable) {
      final Element child = getDocument().createElement(tag);
      element.appendChild(child);

      final DomBuilder domBuilder = new DomBuilder(child);
      for (final BiConsumer<E, DomBuilder> configurer : configurers) {
        configurer.accept(e, domBuilder);
      }
    }
    return this;
  }

  @SafeVarargs
  public final <E> DomBuilder $(E e, BiConsumer<E, DomBuilder>... configurers) {
    for (final BiConsumer<E, DomBuilder> configurer : configurers) {
      configurer.accept(e, this);
    }
    return this;
  }

  @SafeVarargs
  public final <E> DomBuilder $(E e, Predicate<E> filter, BiConsumer<E, DomBuilder>... configurers) {
    if (filter.test(e)) {
      return $(e, configurers);
    } else {
      return this;
    }
  }

  @SafeVarargs
  public final <E> DomBuilder $(Supplier<E> supplier, BiConsumer<E, DomBuilder>... configurers) {
    final E e = supplier.get();
    for (final BiConsumer<E, DomBuilder> configurer : configurers) {
      configurer.accept(e, this);
    }
    return this;
  }

  @SafeVarargs
  public final <E> DomBuilder $(Supplier<E> supplier, Predicate<E> filter, BiConsumer<E, DomBuilder>... configurers) {
    final E e = supplier.get();
    if (filter.test(e)) {
      return $(e, configurers);
    } else {
      return this;
    }
  }

  public final DomBuilder $(Runnable action) {
    action.run();
    return this;
  }

  public final DomBuilder $(Consumer<DomBuilder> configurer) {
    configurer.accept(this);
    return this;
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

  public DomBuilder stylesheet(String href) {
    return e("link", Map.of("type", "text/css", "rel", "stylesheet", "href", href));
  }

  public DomBuilder script(String src) {
    return e("script", Map.of("type", "text/javascript", "src", src));
  }

  public DomBuilder meta(String name, String content) {
    return e("meta", Map.of("name", name, "content", content));
  }

  @SafeVarargs
  public final DomBuilder form(String action, String method, String id, String cls, Consumer<DomBuilder>... configurers) {
    return e("form", Map.of("action", action, "method", method, "id", id, "class", cls), configurers);
  }

  protected TransformerFactory transformerFactory() {
    final TransformerFactory transformerFactory = TransformerFactory.newDefaultInstance();
    transformerFactory.setAttribute("indent-number", 2);
    return transformerFactory;
  }

  protected Transformer transformer(TransformerFactory factory) {
    try {
      return factory.newTransformer();
    } catch (Exception impossibleException) {
      throw new IllegalStateException(impossibleException);
    }
  }

  public DomBuilder write(Result result) throws IOException {
    try {
      final Transformer transformer = transformer(transformerFactory());
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
