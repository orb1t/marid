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
import org.marid.app.spring.LoggingPostProcessor;
import org.marid.appcontext.session.SessionConfiguration;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class Sessions {

  private final ConcurrentHashMap<Session, GenericApplicationContext> sessionContexts = new ConcurrentHashMap<>();
  private final Logger logger;

  public Sessions(Logger logger) {
    this.logger = logger;
  }

  @Autowired
  public void init(SessionManager sessionManager) {
    sessionManager.registerSessionListener(new SessionListener() {
      @Override
      public void sessionCreated(Session session, HttpServerExchange exchange) {
        sessionContexts.computeIfAbsent(session, s -> {
          try {
            final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
            context.setAllowBeanDefinitionOverriding(false);
            context.setAllowCircularReferences(false);
            context.setId(s.getId());
            context.setDisplayName(s.getId() + " (" + Instant.ofEpochMilli(s.getCreationTime()) + ")");
            context.getBeanFactory().addBeanPostProcessor(new LoggingPostProcessor());
            context.registerBean(SessionConfiguration.class, () -> new SessionConfiguration(s));
            context.refresh();
            context.start();
            logger.info("Created session {}", s.getId());
            return context;
          } catch (Exception x) {
            logger.warn("Unable to create session {} context", s.getId(), x);
            return null;
          }
        });
      }

      @Override
      public void sessionDestroyed(Session session, HttpServerExchange exchange, SessionDestroyedReason reason) {
        logger.info("Destroyed session {}", session.getId());
        sessionContexts.computeIfPresent(session, (s, old) -> {
          try (old) {
            logger.info("Closing {}", s.getId());
          } catch (Exception x) {
            logger.warn("Unable to close {}", s.getId(), x);
          }
          return null;
        });
      }
    });
  }
}
