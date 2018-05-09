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
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.server.handlers.CanonicalPathHandler;
import io.undertow.server.handlers.RedirectHandler;
import io.undertow.server.handlers.form.EagerFormParsingHandler;
import io.undertow.server.handlers.form.FormEncodedDataDefinition;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.server.handlers.sse.ServerSentEventHandler;
import org.marid.app.annotation.Handler;
import org.marid.app.auth.MaridProfileManager;
import org.marid.app.auth.MaridWebContext;
import org.marid.app.html.StdLib;
import org.marid.app.http.HttpExecutor;
import org.marid.xml.HtmlBuilder;
import org.pac4j.core.config.Config;
import org.pac4j.core.engine.DefaultCallbackLogic;
import org.pac4j.core.engine.DefaultLogoutLogic;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Handlers {

  @Bean
  @Handler(path = "/sse", authorizer = "user")
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
  @Handler(path = "/", processUnauthorized = true)
  public HttpHandler authListHandler(HttpExecutor executor) {
    return exchange -> {
      CHECK_PROFILES: {
        if (exchange.getSecurityContext() == null) {
          break CHECK_PROFILES;
        }
        if (exchange.getSecurityContext().getAuthenticatedAccount() == null) {
          break CHECK_PROFILES;
        }
        if (exchange.getSecurityContext().getAuthenticatedAccount().getRoles().contains("ROLE_USER")) {
          new RedirectHandler("menu").handleRequest(exchange);
          return;
        }
      }
      final HttpHandler httpHandler = executor.handler(StdLib.class, HtmlBuilder::new, (c, builder) -> builder
          .head(head -> head
              .title(c.s("maridIde"))
              .link("icon", "/marid-icon.gif", "image/gif")
              .meta("google", "notranslate")
              .meta("viewport", "width=device-width, initial-scale=1")
              .stylesheet("/public/login.css")
          )
          .body(body -> body
              .img(100, 100, "/marid-icon.gif?size=100")
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
      httpHandler.handleRequest(exchange);
    };
  }

  @Bean
  @Handler(path = "/menu", authorizer = "user")
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
  @Handler(path = "/callback", processUnauthorized = true, secure = false)
  public HttpHandler callbackHandler(Config config) {
    final var factory = FormParserFactory.builder().addParser(new FormEncodedDataDefinition()).build();
    final var formHandler = new EagerFormParsingHandler(factory);
    final var callbackLogic = new DefaultCallbackLogic<Object, MaridWebContext>();
    callbackLogic.setProfileManagerFactory(MaridProfileManager::new);

    final HttpHandler httpHandler = exchange -> {
      final var context = new MaridWebContext(exchange);
      callbackLogic.perform(context, config, (code, ctx) -> null, "/", true, false, null, null);
    };
    formHandler.setNext(httpHandler);
    return new BlockingHandler(formHandler);
  }

  @Bean
  @Handler(path = "/logout")
  public HttpHandler logoutHandler(Config config) {
    final var logoutLogic = new DefaultLogoutLogic<Object, MaridWebContext>();
    logoutLogic.setProfileManagerFactory(MaridProfileManager::new);
    return exchange -> {
      final var context = new MaridWebContext(exchange);
      logoutLogic.perform(context, config, (code, ctx) -> null, "/", null, null, null, null);
    };
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
