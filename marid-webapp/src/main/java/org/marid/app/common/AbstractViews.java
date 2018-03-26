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
import org.marid.app.spring.ContextUtils;
import org.marid.app.spring.LoggingPostProcessor;
import org.marid.app.util.HandlerPath;
import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public abstract class AbstractViews implements HttpHandler {

  public static final String VIEW_KEY = "viewHandlerPath";

  private final Logger logger;
  private final Sessions sessions;
  private final ConcurrentSkipListMap<HandlerPath, GenericApplicationContext> views = new ConcurrentSkipListMap<>();
  private final HashMap<HandlerPath, Class<?>> viewTemplates = new HashMap<>();

  public AbstractViews(Logger logger, Sessions sessions) {
    this.logger = logger;
    this.sessions = sessions;
  }

  public void register(String path, Class<?> viewTemplate) {
    viewTemplates.put(new HandlerPath(path), viewTemplate);
  }

  public void clean(HandlerPath path) {
    views.computeIfPresent(path, (k, old) -> {
      final HandlerPath from = k.resolve("");
      if (views.tailMap(from, false).isEmpty()) {
        try (old) {
          return null;
        }
      } else {
        return old;
      }
    });
  }

  @Override
  public void handleRequest(HttpServerExchange exchange) throws Exception {

    final Session session = sessions.getSession(exchange);
    if (session == null) {
      logger.error("Unable to find session for {}", exchange);
      return;
    }

    final GenericApplicationContext sessionContext = sessions.getSessionContext(session);
    if (sessionContext == null) {
      logger.error("Unable to find session context for session {}", session.getId());
      return;
    }

    final HandlerPath path = new HandlerPath(exchange.getRelativePath());
    if (!path.isGround()) {
      logger.error("Invalid path {}", path);
      return;
    }

    if (exchange.getQueryParameters().containsKey("clean")) {
      clean(path);
      return;
    }

    final GenericApplicationContext delegatedContext = views.computeIfAbsent(path, k -> {
      final ApplicationListener<ContextClosedEvent> closeListener = ev -> views.remove(k);
      final int len = k.getComponentCount();
      final AtomicReference<GenericApplicationContext> parent = new AtomicReference<>(sessionContext);
      final AtomicReference<HandlerPath> templatePath = new AtomicReference<>(HandlerPath.EMPTY);
      final Function<HandlerPath, GenericApplicationContext> contextSupplier = p -> views.computeIfAbsent(p, pk -> {
        final Class<?> view = resolveView(templatePath);
        if (view != null) {
          final AnnotationConfigApplicationContext context = ContextUtils.context(parent.get());
          context.setId(pk.toString());
          context.setDisplayName(Objects.requireNonNull(context.getId()));
          context.getBeanFactory().addBeanPostProcessor(new LoggingPostProcessor());
          context.getEnvironment().getPropertySources().addLast(new MapPropertySource("child", Map.of(VIEW_KEY, pk)));
          context.register(view);
          context.addApplicationListener(closeListener);
          context.refresh();
          context.start();
          return context;
        } else {
          logger.error("Unable to create context for {}", pk);
          return null;
        }
      });
      for (int i = 1; i < len; i++) {
        final HandlerPath p = k.subPath(i);
        templatePath.set(templatePath.get().resolve(p.last()));
        final GenericApplicationContext c = contextSupplier.apply(p);
        if (c == null) {
          return null;
        } else {
          parent.set(c);
        }
      }
      templatePath.set(templatePath.get().resolve(k.last()));
      return contextSupplier.apply(k);
    });

    if (delegatedContext != null) {
      final HttpHandler delegate;
      try {
        delegate = delegatedContext.getBean(HttpHandler.class);
      } catch (BeansException x) {
        logger.error("Unable to find handler in {}", delegatedContext);
        return;
      }
      delegate.handleRequest(exchange);
    }
  }

  private Class<?> resolveView(AtomicReference<HandlerPath> path) {
    final Class<?> result = viewTemplates.get(path.get());
    if (result == null) {
      path.set(path.get().parent().resolve("*"));
      return viewTemplates.get(path.get());
    } else {
      return result;
    }
  }
}
