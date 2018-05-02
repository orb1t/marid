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

import io.undertow.attribute.ConstantExchangeAttribute;
import io.undertow.security.api.SecurityContext;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.CanonicalPathHandler;
import io.undertow.server.handlers.RedirectHandler;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.server.handlers.sse.ServerSentEventHandler;
import org.marid.app.annotation.Handler;
import org.marid.app.html.StdLib;
import org.marid.app.http.HttpExecutor;
import org.marid.xml.HtmlBuilder;
import org.pac4j.core.config.Config;
import org.pac4j.undertow.handler.LogoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class Handlers {

  @Bean
  @Handler(path = "/sse", processUnauthorized = true)
  public ServerSentEventHandler sseHandler() {
    return new ServerSentEventHandler();
  }

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
  public HttpHandler authListHandler(HttpExecutor executor) {
    return exchange -> executor.html(exchange, (c, builder) -> builder
        .$e("head", head -> head
            .title(c.s("maridIde"))
            .link("icon", "/marid-icon.gif", "image/gif")
            .meta("google", "notranslate")
            .meta("viewport", "width=device-width, initial-scale=1")
            .stylesheet("/public/login.css")
        )
        .$e("body", body -> body
            .$e("img", Map.of("src", "/marid-icon.gif?size=100", "width", 100, "height", 100))
            .div("", "adBody", list -> list
                .div("", "header", c.s("maridIde"))
                .div("", "ad", c.s("spiritDrivenDevelopment"))
                .div("", "auth", auth -> List.of("google", "facebook", "twitter").forEach(e -> {
                  auth.$c(e);
                  auth.a("", "/" + e + ".html", "", a -> a.img(32, 32, "/public/" + e + ".svg"));
                }))
            )
        )
    );
  }

  @Bean
  public HttpHandler mainMenuHandler(HttpExecutor executor) {
    return executor.handler(StdLib.class, HtmlBuilder::new, (c, b) -> b
        .$(() -> c.stdHead(b, h -> h
            .stylesheet("/user/css/index.css")
            .title(c.s("maridMenu")))
        )
        .body(body -> body
            .div("list-group", list -> list
                .div("list-group-item", header -> header
                    .img(32, 32, "/marid-icon.gif")
                    .cspan("ml-3", c.s("maridMenu"))
                )
                .div("list-group-item list-group-item-primary", "sessionHeader", c.s("session"))
                .a("list-group-item list-group-item-action", "/user/view/prefs", c.s("preferences"))
                .a("list-group-item list-group-item-action", "/logout", c.s("logOut"))
                .div("list-group-item list-group-item-primary", "sessionHeader", c.s("cellars"))
                .a("list-group-item list-group-item-action", "/view/cellars/manage.html", c.s("manage"))
            )
            .$(() -> c.scripts(body))
        )
    );
  }

  @Bean
  @Handler(path = "/", processUnauthorized = true)
  public HttpHandler loginPage(HttpHandler authListHandler, HttpHandler mainMenuHandler) {
    return exchange -> {
      final SecurityContext context = exchange.getSecurityContext();
      if (context == null) {
        authListHandler.handleRequest(exchange);
      } else {
        mainMenuHandler.handleRequest(exchange);
      }
    };
  }

  @Bean
  @Handler(path = "/logout")
  public HttpHandler logoutHandler(Config config) {
    return new LogoutHandler(config, "/");
  }

  @Bean
  @Handler(path = "/google.html", client = "Google2Client")
  public HttpHandler googlePage() {
    return new RedirectHandler(new ConstantExchangeAttribute("/"));
  }

  @Bean
  @Handler(path = "/facebook.html", client = "FacebookClient")
  public HttpHandler facebookPage() {
    return new RedirectHandler(new ConstantExchangeAttribute("/"));
  }
}
