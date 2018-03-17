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
import org.marid.xml.HtmlBuilder;
import org.pac4j.core.config.Config;
import org.pac4j.undertow.handler.CallbackHandler;
import org.pac4j.undertow.handler.SecurityHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.xml.transform.stream.StreamResult;
import java.util.Map;

@Component
public class Handlers {

  @Bean
  @Handler(path = "/callback")
  public HttpHandler callbackHandler(Config authConfig) {
    return CallbackHandler.build(authConfig, null, true);
  }

  @Bean
  @Handler(path = "/pub", exact = false)
  public HttpHandler pubResourcesHandler(ResourceManager pubResourceManager) {
    return new CanonicalPathHandler(new ResourceHandler(pubResourceManager));
  }

  @Bean
  @Handler(path = "/user", exact = false)
  public HttpHandler userResourcesHandler(ResourceManager userResourceManager, Config authConfig) {
    return SecurityHandler.build(new ResourceHandler(userResourceManager), authConfig, null, "user");
  }

  @Bean
  @Handler(path = "/admin", exact = false)
  public HttpHandler adminResourceHandler(ResourceManager adminResourceManager, Config authConfig) {
    return SecurityHandler.build(new ResourceHandler(adminResourceManager), authConfig, null, "admin");
  }

  @Bean
  @Handler(path = "/")
  public HttpHandler loginPage(HttpExecutor executor) {
    return exchange -> executor.html(exchange).with(exchange, (i, o) -> new HtmlBuilder()
        .child("head")
        .child("body", b -> {
          b.child("a", Map.of("href", "/google.html"), bb -> bb.text("Google"));
          b.child("a", Map.of("href", "/facebook.html"), bb -> bb.text("Facebook"));
        })
        .write(new StreamResult(o)));
  }

  @Bean
  @Handler(path = "/google.html")
  public HttpHandler googlePage(Config authConfig, HttpExecutor executor) {
    return SecurityHandler.build(exchange -> executor.html(exchange).with(exchange, (i, o) -> new HtmlBuilder()
        .child("head")
        .child("body", b -> {
          b.child("p", bb -> bb.text("Done!"));
        })
        .write(new StreamResult(o))), authConfig, "GoogleOidcClient");
  }
}
