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

import org.intellij.lang.annotations.Language;
import org.w3c.dom.Element;

import java.util.Map;
import java.util.function.Consumer;

public abstract class HtmlAbstractBuilder<B extends HtmlAbstractBuilder<B>> extends DomBuilder<B> {

  public HtmlAbstractBuilder(Element element) {
    super(element);
  }

  public B stylesheet(
      @Language(value = "HTML", prefix = "<link rel='stylesheet' type='text/css' href='", suffix = "'>") String href) {
    return link("stylesheet", href, "text/css");
  }

  public B stylesheet(
      @Language(value = "HTML", prefix = "<link rel='stylesheet' type='text/css' href='", suffix = "'>") String href,
      String integrity) {
    return $e("link", Map.of(
        "rel", "stylesheet",
        "href", href,
        "type", "text/css",
        "integrity", integrity,
        "crossorigin", "anonymous"
    ));
  }

  public B script(@Language(value = "HTML", prefix = "<script src='", suffix = "'>") String src) {
    return $e("script", Map.of("type", "text/javascript", "src", src));
  }

  public B script(@Language(value = "HTML", prefix = "<script src='", suffix = "'>") String src,
                  @Language(value = "HTML", prefix = "<script integrity='", suffix = "'>") String integrity) {
    return $e("script", Map.of(
        "type", "text/javascript",
        "src", src,
        "integrity", integrity,
        "crossorigin", "anonymous"
    ));
  }

  public B meta(String name, String content) {
    return $e("meta", Map.of("name", name, "content", content));
  }

  public B link(@Language(value = "HTML", prefix = "<link rel='", suffix = "'>") String rel,
                @Language(value = "HTML", prefix = "<link href='", suffix = "'>") String href,
                @Language(value = "HTML", prefix = "<link type='", suffix = "'>") String type) {
    return $e("link", Map.of("rel", rel, "href", href, "type", type));
  }

  @SafeVarargs
  public final B form(@Language(value = "HTML", prefix = "<form action='", suffix = "'/>") String action,
                      @Language(value = "HTML", prefix = "<form method='", suffix = "'/>") String method,
                      @Language(value = "HTML", prefix = "<form id='", suffix = "'/>") String id,
                      @Language(value = "HTML", prefix = "<form class='", suffix = "'/>") String cls,
                      Consumer<B>... configurers) {
    return $e("form", Map.of("action", action, "method", method, "id", id, "class", cls), configurers);
  }

  @SafeVarargs
  public final B div(@Language(value = "HTML", prefix = "<div class='", suffix = "'/>") String cls,
                     @Language(value = "HTML", prefix = "<div id='", suffix = "'/>") String id,
                     String txt,
                     Consumer<B>... configurers) {
    return $e("div", txt, cls.isEmpty() ? Map.of("id", id) : Map.of("class", cls, "id", id), configurers);
  }

  @SafeVarargs
  public final B div(@Language(value = "HTML", prefix = "<div class='", suffix = "'/>") String cls,
                     @Language(value = "HTML", prefix = "<div id='", suffix = "'/>") String id,
                     Consumer<B>... configurers) {
    return $e("div", cls.isEmpty() ? Map.of("id", id) : Map.of("class", cls, "id", id), configurers);
  }

  @SafeVarargs
  public final B div(@Language(value = "HTML", prefix = "<div class='", suffix = "'/>") String cls,
                     Consumer<B>... configurers) {
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
  public final B body(@Language(value = "HTML", prefix = "<body class='", suffix = "'/>") String cls,
                      Consumer<B>... configurers) {
    return $e("body", Map.of("class", cls), configurers);
  }

  @SafeVarargs
  public final B a(@Language(value = "HTML", prefix = "<a class='", suffix = "'/>") String cls,
                   @Language(value = "HTML", prefix = "<a href='", suffix = "'/>") String href,
                   String text, Consumer<B>... configurers) {
    final var attrs = cls.isEmpty() ? Map.of("href", href) : Map.of("class", cls, "href", href);
    return text.isEmpty() ? $e("a", attrs, configurers) : $e("a", text, attrs, configurers);
  }

  public B title(String title) {
    return $e("title", title, Map.of());
  }

  public B title(String format, Object... args) {
    return $e(String.format(format, args));
  }

  public B img(Object width,
               Object height,
               @Language(value = "HTML", prefix = "<img src='", suffix = "'/>") String src) {
    return $e("img", Map.of("width", width, "height", height, "src", src));
  }

  public B icon(Object size, @Language(value = "HTML", prefix = "<img src='", suffix = "'/>") String src) {
    return img(size, size, src);
  }

  public B span(Object text) {
    return $e("span", text.toString(), Map.of());
  }

  public B span(String format, Object... args) {
    return $e("span", String.format(format, args), Map.of());
  }

  public B cspan(@Language(value = "HTML", prefix = "<span class='", suffix = "'/>") String cls, Object text) {
    return $e("span", text.toString(), Map.of("class", cls));
  }

  public B cspan(@Language(value = "HTML", prefix = "<span class='", suffix = "'/>") String cls,
                 String format,
                 Object... args) {
    return cspan(cls, String.format(format, args));
  }

  public B i(@Language(value = "HTML", prefix = "<i class='", suffix = "'/>") String cls) {
    return $e("i", Map.of("class", cls));
  }

  public B label(String text) {
    return $e("label", text, Map.of());
  }

  public B label(String text, @Language(value = "HTML", prefix = "<label for='", suffix = "'/>") String forName) {
    return $e("label", text, Map.of("for", forName));
  }

  @SafeVarargs
  public final B input(@Language(value = "HTML", prefix = "<input name='", suffix = "'/>") String name,
                       @Language(value = "HTML", prefix = "<input type='", suffix = "'/>") String type,
                       @Language(value = "HTML", prefix = "<input class='", suffix = "'/>") String cls,
                       String placeholder,
                       String value,
                       Consumer<B>... configurers) {
    final var map = value.isEmpty()
        ? Map.of("name", name, "type", type, "placeholder", placeholder, "class", cls)
        : Map.of("name", name, "type", type, "placeholder", placeholder, "value", value, "class", cls);
    return $e("input", map, configurers);
  }

  public B submitButton(@Language(value = "HTML", prefix = "<input class='", suffix = "'/>") String cls,
                        String value) {
    return $e("input", Map.of("type", "submit", "value", value, "class", cls));
  }

  @SafeVarargs
  public final B button(@Language(value = "HTML", prefix = "<button class='", suffix = "'/>") String cls,
                        Consumer<B>... configurers) {
    return $e("button", Map.of("class", cls), configurers);
  }

  public B h5(@Language(value = "HTML", prefix = "<h5 class='", suffix = "'/>") String cls, String text) {
    return $e("h5", text, Map.of("class", cls));
  }
}
