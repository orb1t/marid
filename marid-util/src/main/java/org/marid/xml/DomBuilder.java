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

import org.marid.misc.Casts;
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
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.marid.misc.StringUtils.stringOrNull;

public abstract class DomBuilder<B extends DomBuilder<B>> {

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

  protected B self() {
    return Casts.cast(this);
  }

  public B $t(String text) {
    element.setTextContent(text);
    return self();
  }

  public B $t(String text, Object... args) {
    return $t(String.format(text, args));
  }

  public B $a(String attr, Object value) {
    element.setAttribute(attr, stringOrNull(value));
    return self();
  }

  public B $a(Map<String, ?> attrs) {
    attrs.forEach(this::$a);
    return self();
  }

  public B $c(String commentText) {
    element.appendChild(getDocument().createComment(commentText));
    return self();
  }

  public B $c(String commentText, Object... args) {
    return $c(String.format(commentText, args));
  }

  public B $i(String text) {
    element.appendChild(getDocument().createTextNode(text));
    return self();
  }

  public B $i(String text, Object... args) {
    return $i(String.format(text, args));
  }

  protected abstract B child(Element element);

  @SafeVarargs
  public final B $e(String tag, String text, Map<String, ?> attrs, Consumer<B>... configurers) {
    final Element child = getDocument().createElement(tag);

    final B domBuilder = child(child).$a(attrs);

    domBuilder.$t(text);
    for (final Consumer<B> childConfigurer : configurers) {
      childConfigurer.accept(domBuilder);
    }

    element.appendChild(child);

    return self();
  }

  @SafeVarargs
  public final B $e(String tag, Map<String, ?> attrs, Consumer<B>... childConfigurers) {
    final Element child = getDocument().createElement(tag);

    final B domBuilder = child(child).$a(attrs);
    for (final Consumer<B> childConfigurer : childConfigurers) {
      childConfigurer.accept(domBuilder);
    }

    element.appendChild(child);

    return self();
  }

  @SafeVarargs
  public final B $e(String tag, Consumer<B>... childConfigurers) {
    final Element child = getDocument().createElement(tag);

    final B domBuilder = child(child);
    for (final Consumer<B> childConfigurer : childConfigurers) {
      childConfigurer.accept(domBuilder);
    }

    element.appendChild(child);

    return self();
  }

  @SafeVarargs
  public final <E> B $(E e, Consumer<E>... configurers) {
    for (final Consumer<E> configurer : configurers) {
      configurer.accept(e);
    }
    return self();
  }

  @SafeVarargs
  public final <E> B $(E e, Predicate<E> filter, Consumer<E>... configurers) {
    if (filter.test(e)) {
      return $(e, configurers);
    } else {
      return self();
    }
  }

  @SafeVarargs
  public final <E> B $(Supplier<E> supplier, Consumer<E>... configurers) {
    final E e = supplier.get();
    for (final Consumer<E> configurer : configurers) {
      configurer.accept(e);
    }
    return self();
  }

  @SafeVarargs
  public final <E> B $(Supplier<E> supplier, Predicate<E> filter, Consumer<E>... configurers) {
    final E e = supplier.get();
    if (filter.test(e)) {
      return $(e, configurers);
    } else {
      return self();
    }
  }

  public final B $(Runnable action) {
    action.run();
    return self();
  }

  public final B $if(BooleanSupplier conditionSupplier, Runnable... configurers) {
    if (conditionSupplier.getAsBoolean()) {
      for (final Runnable configurer : configurers) {
        configurer.run();
      }
    }
    return self();
  }

  public final B $if(boolean condition, Runnable... configurers) {
    if (condition) {
      for (final Runnable configurer : configurers) {
        configurer.run();
      }
    }
    return self();
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
