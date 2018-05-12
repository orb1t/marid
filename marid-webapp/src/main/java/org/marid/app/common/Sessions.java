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

import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.Session;
import io.undertow.server.session.SessionListener;
import io.undertow.server.session.SessionManager;
import org.marid.app.spring.ContextUtils;
import org.marid.app.spring.LoggingPostProcessor;
import org.marid.app.util.ExchangeHelper;
import org.marid.appcontext.session.SessionConfiguration;
import org.pac4j.core.context.Pac4jConstants;
import org.slf4j.Logger;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import static io.undertow.util.Sessions.getSession;

@Component
public class Sessions {

  private static final String CONTEXT_KEY = "CHILD_CONTEXT";

  private final Logger logger;
  private final GenericApplicationContext parent;

  public Sessions(Logger logger, GenericApplicationContext parent, SessionManager sessionManager) {
    this.logger = logger;
    this.parent = parent;

    sessionManager.registerSessionListener(new MaridSessionListener());
  }

  public Session get(HttpServerExchange exchange) {
    return getSession(exchange);
  }

  public GenericApplicationContext getSessionContext(Session session) {
    return (GenericApplicationContext) session.getAttribute(CONTEXT_KEY);
  }

  private class MaridSessionListener implements SessionListener {
    @Override
    public void sessionCreated(Session session, HttpServerExchange exchange) {
      logger.info("Created {}", session);
      final Map map = (Map) session.getAttribute(Pac4jConstants.USER_PROFILES);
      if (map != null && !map.isEmpty() && session.getAttribute(CONTEXT_KEY) == null) {
        create(session);
      }
    }

    @Override
    public void sessionDestroyed(Session session, HttpServerExchange exchange, SessionDestroyedReason reason) {
      logger.info("Destroyed {} ({})", session, reason);
      destroy(session);
    }

    @Override
    public void attributeAdded(Session session, String name, Object value) {
      switch (name) {
        case Pac4jConstants.USER_PROFILES:
          create(session);
          break;
      }
    }

    @Override
    public void attributeRemoved(Session session, String name, Object oldValue) {
      switch (name) {
        case Pac4jConstants.USER_PROFILES:
          destroy(session);
          break;
      }
    }

    @Override
    public void attributeUpdated(Session session, String name, Object newValue, Object oldValue) {
      switch (name) {
        case Pac4jConstants.USER_PROFILES:
          if (!Objects.equals(oldValue, newValue)) {
            destroy(session);
            if (newValue instanceof Map && !((Map) newValue).isEmpty()) {
              create(session);
            }
          }
          break;
      }
    }

    private void create(Session session) {
      session.setAttribute(CONTEXT_KEY, ContextUtils.context(parent, child -> {
        final ApplicationListener<ContextClosedEvent> closeListener = event -> {
          if (event.getApplicationContext() == child) {
            session.removeAttribute(CONTEXT_KEY);
          }
        };
        child.setId(session.getId());
        child.setDisplayName(session.getId() + " (" + Instant.ofEpochMilli(session.getCreationTime()) + ")");
        child.getBeanFactory().registerSingleton("session", session);
        child.getBeanFactory().registerSingleton("userProfile", ExchangeHelper.userProfile(session));
        child.register(SessionConfiguration.class);
        child.addApplicationListener(closeListener);
        child.refresh();
        child.start();
        logger.info("Created session {}", session.getId());
      }));
    }

    private void destroy(Session session) {
      final GenericApplicationContext context = (GenericApplicationContext) session.removeAttribute(CONTEXT_KEY);
      if (context != null) {
        try (context) {
          logger.info("Destroying {}: {}", session, session.getId());
        }
      }
    }
  }
}
