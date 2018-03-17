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

package org.marid.app.handlers;

import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.CanonicalPathHandler;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.server.handlers.resource.ResourceManager;
import org.marid.app.annotation.Handler;
import org.marid.app.http.HttpExecutor;
import org.marid.image.MaridIcon;
import org.marid.xml.HtmlBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.nio.ByteBuffer;
import java.util.Map;

import static org.marid.app.util.ExchangeHelper.queryParams;

@Component
public class Handlers {

  @Bean
  public HttpHandler pubResourcesHandler(ResourceManager pubResourceManager) {
    return new CanonicalPathHandler(new ResourceHandler(pubResourceManager));
  }

  @Bean
  @Handler(path = "/user", exact = false, authorizer = "user", safePath = true)
  public HttpHandler userResourcesHandler(ResourceManager userResourceManager) {
    return new ResourceHandler(userResourceManager);
  }

  @Bean
  @Handler(path = "/admin", exact = false, authorizer = "admin", safePath = true)
  public HttpHandler adminResourceHandler(ResourceManager adminResourceManager) {
    return new ResourceHandler(adminResourceManager);
  }

  @Bean
  @Handler(path = "/marid-icon.gif", secure = false)
  public HttpHandler maridIconHandler() {
    return exchange -> {
      try {
        final int size = queryParams(exchange, "size").mapToInt(Integer::parseInt).findFirst().orElse(32);
        final BufferedImage image = MaridIcon.getImage(size, Color.GREEN);

        final ByteArrayOutputStream stream = new ByteArrayOutputStream(16384);
        try (stream) {
          ImageIO.write(image, "GIF", stream);
        }

        exchange.setStatusCode(HttpURLConnection.HTTP_OK);
        exchange.getResponseSender().send(ByteBuffer.wrap(stream.toByteArray()));
      } catch (Exception x) {
        exchange.setStatusCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
        exchange.endExchange();
      }
    };
  }

  @Bean
  @Handler(path = "/", processUnauthorized = true)
  public HttpHandler loginPage(HttpExecutor executor) {
    return exchange -> {
      executor.html(exchange).with(exchange, (i, o) -> new HtmlBuilder()
          .child("head")
          .child("body", b -> {
            b.child("a", Map.of("href", "/google.html"), bb -> bb.text("Google"));
            b.child("a", Map.of("href", "/facebook.html"), bb -> bb.text("Facebook"));
          })
          .write(new StreamResult(o)));
    };
  }

  @Bean
  @Handler(path = "/google.html", client = "GoogleOidcClient")
  public HttpHandler googlePage(HttpExecutor executor) {
    return exchange -> executor.html(exchange).with(exchange, (i, o) -> new HtmlBuilder()
        .child("head")
        .child("body", b -> {
          b.child("p", bb -> bb.text("Done!"));
        })
        .write(new StreamResult(o)));
  }
}
