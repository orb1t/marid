/*-
 * #%L
 * marid-webapp
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

package org.marid.app.http;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import org.marid.app.common.Sessions;
import org.marid.app.html.BaseLib;
import org.marid.io.IOConsumer;
import org.marid.xml.HtmlAbstractBuilder;
import org.marid.xml.HtmlBuilder;
import org.marid.xml.HtmlFragmentBuilder;
import org.slf4j.Logger;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.stereotype.Component;

import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.springframework.beans.factory.config.AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR;

@Component
public class HttpExecutor {

  private final Logger logger;
  private final Sessions sessions;

  public HttpExecutor(Logger logger, Sessions sessions) {
    this.logger = logger;
    this.sessions = sessions;
  }

  public <L extends BaseLib> HttpHandler handler(Class<L> lib, Consumer<L> code) {
    return ex -> {
      final var factory = new DefaultListableBeanFactory();
      final var session = sessions.get(ex);
      if (session != null) {
        final var parent = sessions.getSessionContext(session);
        if (parent != null) {
          factory.setParentBeanFactory(parent.getBeanFactory());
        }
      }
      factory.registerSingleton("exchange", ex);
      final L l = lib.cast(factory.createBean(lib, AUTOWIRE_CONSTRUCTOR, true));
      code.accept(l);
    };
  }

  public <L extends BaseLib, B extends HtmlAbstractBuilder<B>> HttpHandler handler(Class<L> lib,
                                                                                   Supplier<B> builder,
                                                                                   BiConsumer<L, B> code) {
    return handler(lib, l -> {
      final B b = builder.get();
      code.accept(l, b);
      l.getExchange().dispatch(() -> {
        l.getExchange().startBlocking();
        try {
          b.write(new StreamResult(l.getOut()));
        } catch (IOException x) {
          throw new UncheckedIOException(x);
        } finally {
          l.getExchange().endExchange();
        }
      });
    });
  }

  public void html(HttpServerExchange exchange, BiConsumer<HttpContext, HtmlBuilder> html) {
    exchange.getResponseHeaders().add(new HttpString("Content-Type"), "text/html; charset=UTF-8");
    this.with(exchange, c -> {
      final HtmlBuilder builder = new HtmlBuilder();
      html.accept(c, builder);
      builder.write(new StreamResult(c.getOut()));
    });
  }

  public void fragment(HttpServerExchange exchange,
                       String tag,
                       Map<String, ?> attrs,
                       BiConsumer<HttpContext, HtmlFragmentBuilder> fragment) {
    exchange.getResponseHeaders().add(new HttpString("Content-Type"), "text/html; charset=UTF-8");
    this.with(exchange, c -> {
      final HtmlFragmentBuilder builder = new HtmlFragmentBuilder(tag, attrs);
      fragment.accept(c, builder);
      builder.write(new StreamResult(c.getOut()));
    });
  }

  public void with(HttpServerExchange exchange, IOConsumer<HttpContext> consumer) {
    exchange.dispatch(() -> {
      exchange.startBlocking();
      try {
        consumer.ioAccept(new HttpContext(exchange));
      } catch (Exception x) {
        logger.warn("Unable to process {}", exchange, x);
        exchange.setStatusCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
      } finally {
        exchange.endExchange();
      }
    });
  }
}
