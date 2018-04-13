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

import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public abstract class HtmlAbstractBuilder<B extends HtmlAbstractBuilder<B>> extends DomBuilder<B> {

  public HtmlAbstractBuilder(Element element) {
    super(element);
  }

  public B stylesheet(String href) {
    return link("stylesheet", href, "text/css");
  }

  public B script(String src) {
    return $e("script", Map.of("type", "text/javascript", "src", src));
  }

  public B meta(String name, String content) {
    return $e("meta", Map.of("name", name, "content", content));
  }

  public B link(String rel, String href, String type) {
    return $e("link", Map.of("rel", rel, "href", href, "type", type));
  }

  @SafeVarargs
  public final B form(String action, String method, String id, String cls, Consumer<B>... configurers) {
    return $e("form", Map.of("action", action, "method", method, "id", id, "class", cls), configurers);
  }

  @SafeVarargs
  public final B div(String cls, String id, String txt, Consumer<B>... configurers) {
    return $e("div", txt, cls.isEmpty() ? Map.of("id", id) : Map.of("class", cls, "id", id), configurers);
  }

  @SafeVarargs
  public final B div(String cls, String id, Consumer<B>... configurers) {
    return $e("div", cls.isEmpty() ? Map.of("id", id) : Map.of("class", cls, "id", id), configurers);
  }

  @SafeVarargs
  public final B div(String cls, Consumer<B>... configurers) {
    return $e("div", Map.of("class", cls), configurers);
  }

  @SafeVarargs
  public final B head(Consumer<B>... configurers) {
    return $e("head", configurers);
  }

  @SafeVarargs
  public final B body(Consumer<B>... configurers) {
    return $e("body", configurers);
  }

  @SafeVarargs
  public final B body(String cls, Consumer<B>... configurers) {
    return $e("body", Map.of("class", cls), configurers);
  }

  @SafeVarargs
  public final B a(String cls, String href, String text, Consumer<B>... configurers) {
    final var attrs = cls.isEmpty() ? Map.of("href", href) : Map.of("class", cls, "href", href);
    return text.isEmpty() ? $e("a", attrs, configurers) : $e("a", text, attrs, configurers);
  }

  public B title(String title) {
    return $e("title", title, Map.of());
  }

  public B title(String format, Object... args) {
    return $e(String.format(format, args));
  }

  public B img(Object width, Object height, String src) {
    return $e("img", Map.of("width", width, "height", height, "src", src));
  }

  public B span(Object text) {
    return $e("span", text.toString(), Map.of());
  }

  public B span(String format, Object... args) {
    return $e("span", String.format(format, args), Map.of());
  }

  public B i(String cls) {
    return $e("i", Map.of("class", cls));
  }

  public B label(String text) {
    return $e("label", text, Map.of());
  }

  public B input(String name, String type, String placeholder, String value) {
    final var map = value.isEmpty()
        ? Map.of("name", name, "type", type, "placeholder", placeholder)
        : Map.of("name", name, "type", type, "placeholder", placeholder, "value", value);
    return $e("input", map);
  }

  public B submitButton(String cls, String value) {
    return $e("input", Map.of("type", "submit", "value", value, "class", cls));
  }
}
