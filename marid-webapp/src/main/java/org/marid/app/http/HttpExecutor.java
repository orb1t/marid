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

import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import org.marid.io.IOBiConsumer;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;

@Component
public class HttpExecutor {

  private final Logger logger;

  public HttpExecutor(Logger logger) {
    this.logger = logger;
  }

  public HttpExecutor html(HttpServerExchange exchange) {
    exchange.getResponseHeaders().add(new HttpString("Content-Type"), "text/html; charset=UTF-8");
    return this;
  }

  public void with(HttpServerExchange exchange, IOBiConsumer<InputStream, OutputStream> consumer) {
    exchange.dispatch(() -> {
      exchange.startBlocking();
      try {
        consumer.ioAccept(exchange.getInputStream(), exchange.getOutputStream());
        exchange.setStatusCode(HttpURLConnection.HTTP_OK);
      } catch (Exception x) {
        logger.warn("Unable to process {}", exchange, x);
        exchange.setStatusCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
      } finally {
        exchange.endExchange();
      }
    });
  }
}
