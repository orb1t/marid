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
import org.marid.app.annotation.Handler;
import org.marid.app.html.StdLib;
import org.marid.app.http.HttpExecutor;
import org.pac4j.core.config.Config;
import org.pac4j.undertow.handler.LogoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

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
  public HttpHandler authListHandler(HttpExecutor executor) {
    return exchange -> executor.html(exchange, (c, builder) -> builder
        .e("head", head -> head
            .e("title", c.s("maridIde"))
            .e("link", Map.of("rel", "icon", "href", "/marid-icon.gif", "type", "image/gif"))
            .meta("google", "notranslate")
            .meta("viewport", "width=device-width, initial-scale=1")
            .stylesheet("/public/login.css")
        )
        .e("body", body -> body
            .e("img", Map.of("src", "/marid-icon.gif?size=100", "width", 100, "height", 100))
            .e("div", Map.of("id", "adBody"), list -> list
                .e("div", Map.of("id", "header"), p -> p.t(c.s("maridIde")))
                .e("div", Map.of("id", "ad"), p -> p.t(c.s("spiritDrivenDevelopment")))
                .e("div", Map.of("id", "auth"), auth -> auth
                    .es("a", List.of("google", "facebook", "twitter"), (e, b) -> b.bc(e)
                        .kv("href", "/" + e + ".html")
                        .e("img", Map.of("src", "/public/" + e + ".svg", "width", 32, "height", 32))
                    )
                )
            )
        )
    );
  }

  @Bean
  public HttpHandler mainMenuHandler(HttpExecutor executor, StdLib stdLib) {
    return exchange -> executor.html(exchange, (c, builder) -> builder
        .$(() -> stdLib.stdHead(builder, c.s("maridMenu"), head -> head.stylesheet("/user/css/index.css")))
        .e("body", Map.of("class", "ui segment"), body -> body
            .e("div", Map.of("class", "ui relaxed divided list"), list -> list
                .e("div", Map.of("class", "item"), item -> item
                    .e("div", Map.of("class", "content"), content -> content
                        .e("div", Map.of("class", "header", "id", "menu"), div -> div
                            .e("img", Map.of("src", "/marid-icon.gif"))
                            .e("span", c.s("maridMenu"))
                        )
                    )
                )
                .e("div", Map.of("class", "item"), item -> item
                    .e("div", Map.of("class", "content"), content -> content
                        .e("div", c.s("session"), Map.of("class", "header"))
                    )
                )
                .e("a", c.s("preferences"), Map.of("class", "item", "href", "/user/view/prefs", "target", "_blank"))
                .e("a", c.s("logOut"), Map.of("class", "item", "href", "/logout"))
                .e("div", Map.of("class", "item"), item -> item
                    .e("div", Map.of("class", "content"), content -> content
                        .e("div", c.s("cellars"), Map.of("class", "header"))
                    )
                )
                .e("a", c.s("manage"), Map.of("class", "item", "href", "/view/cellars/manage.html", "target", "_blank"))
            )
            .$(v -> stdLib.scripts(v))
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
