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

package org.marid.app.common;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.Session;
import org.marid.app.annotation.Handler;
import org.marid.app.spring.ContextUtils;
import org.marid.app.spring.LoggingPostProcessor;
import org.marid.app.util.HandlerPath;
import org.marid.appcontext.session.view.ViewContextResolver;
import org.marid.appcontext.session.view.ViewResolver;
import org.slf4j.Logger;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.stereotype.Component;

import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Handler(path = "/view", exact = false)
@Component
public class Views implements HttpHandler {

  private final Logger logger;
  private final Sessions sessions;
  private final ConcurrentHashMap<HandlerPath, GenericApplicationContext> views = new ConcurrentHashMap<>();

  public Views(Logger logger, Sessions sessions) {
    this.logger = logger;
    this.sessions = sessions;
  }

  public void clean(HandlerPath path) {
    views.computeIfPresent(path, (k, old) -> {
      try (old) {
        return null;
      }
    });
  }

  @Override
  public void handleRequest(HttpServerExchange exchange) {

    final Session session = sessions.get(exchange);
    if (session == null) {
      logger.error("Unable to find session for {}", exchange);
      return;
    }

    final GenericApplicationContext context = sessions.getSessionContext(session);
    if (context == null) {
      logger.error("Unable to find session context for session {}", session.getId());
      return;
    }

    final HandlerPath path = new HandlerPath(exchange.getRelativePath());
    if (path.getComponentCount() == 0) {
      logger.error("Invalid path {}", path);
      return;
    }

    if (exchange.getQueryParameters().containsKey("clean")) {
      clean(path);
      return;
    }

    try {
      final AtomicReference<GenericApplicationContext> ctx = new AtomicReference<>(context);
      for (final ListIterator<String> i = path.iterator(); i.hasNext(); ) {
        final String selector = i.next();
        final HandlerPath current = path.subPath(i.nextIndex());

        if (i.hasNext()) {
          final ViewContextResolver viewContextResolver = viewContextResolver(ctx.get());
          final Class<?> c = viewContextResolver.resolve(selector);
          if (c != null) {
            final ApplicationListener<ContextClosedEvent> contextCloseListener = e -> views.remove(current);
            final MapPropertySource env = new MapPropertySource("viewPropertySource", Map.of("path", current));

            ctx.set(views.computeIfAbsent(current, p -> ContextUtils.context(ctx.get(), child -> {
              child.setDisplayName(current.toString());
              child.setId(child.getDisplayName());
              child.getEnvironment().getPropertySources().addLast(env);
              child.getBeanFactory().addBeanPostProcessor(new LoggingPostProcessor());
              child.register(c);
              child.addApplicationListener(contextCloseListener);
              child.refresh();
              child.start();
            })));
          } else {
            logger.error("Unable to find {} from {}", current, path);
            return;
          }
        } else {
          final ViewResolver viewResolver = viewResolver(ctx.get());
          final HttpHandler httpHandler = viewResolver.resolve(viewName(selector));
          if (httpHandler != null) {
            httpHandler.handleRequest(exchange);
          } else {
            logger.error("Unable to find {} from {}", current, path);
          }
        }
      }
    } catch (Exception x) {
      logger.error("Unable to handle {}", path, x);
    }
  }

  private String viewName(String selector) {
    final int index = selector.indexOf(".html");
    return index >= 0 ? selector.substring(0, index) : selector;
  }

  private ViewContextResolver viewContextResolver(GenericApplicationContext context) {
    return context.getBeansOfType(ViewContextResolver.class).entrySet().stream()
        .filter(e -> context.containsLocalBean(e.getKey()))
        .map(Map.Entry::getValue)
        .findAny()
        .orElseGet(() -> name -> context.getBean(name, Class.class));
  }

  private ViewResolver viewResolver(GenericApplicationContext context) {
    return context.getBeansOfType(ViewResolver.class).entrySet().stream()
        .filter(e -> context.containsLocalBean(e.getKey()))
        .map(Map.Entry::getValue)
        .findAny()
        .orElseGet(() -> name -> context.getBean(name, HttpHandler.class));
  }
}
