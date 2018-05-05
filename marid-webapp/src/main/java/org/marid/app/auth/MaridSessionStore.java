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
package org.marid.app.auth;

import io.undertow.server.session.Session;
import io.undertow.server.session.SessionConfig;
import io.undertow.server.session.SessionManager;
import org.pac4j.core.context.session.SessionStore;

import static java.util.stream.Collectors.toMap;

public class MaridSessionStore implements SessionStore<MaridWebContext> {

  Session get(MaridWebContext context, boolean create) {
    final var manager = context.getExchange().getAttachment(SessionManager.ATTACHMENT_KEY);
    final var config = context.getExchange().getAttachment(SessionConfig.ATTACHMENT_KEY);
    final var session = manager.getSession(context.getExchange(), config);
    return session == null ? (create ? manager.createSession(context.getExchange(), config) : null) : session;
  }

  @Override
  public String getOrCreateSessionId(MaridWebContext context) {
    return get(context, true).getId();
  }

  @Override
  public Object get(MaridWebContext context, String key) {
    final Session session = get(context, false);
    return session == null ? null : session.getAttribute(key);
  }

  @Override
  public void set(MaridWebContext context, String key, Object value) {
    get(context, true).setAttribute(key, value);
  }

  @Override
  public boolean destroySession(MaridWebContext context) {
    final var session = get(context, false);
    if (session != null) {
      session.invalidate(context.getExchange());
      return true;
    } else {
      return false;
    }
  }

  @Override
  public Object getTrackableSession(MaridWebContext context) {
    return get(context, true);
  }

  @Override
  public SessionStore<MaridWebContext> buildFromTrackableSession(MaridWebContext context, Object trackableSession) {
    return new MaridSessionStore() {
      @Override
      Session get(MaridWebContext context, boolean create) {
        return (Session) trackableSession;
      }
    };
  }

  @Override
  public boolean renewSession(MaridWebContext context) {
    final var session = get(context, false);
    if (session == null) {
      get(context, true);
    } else {
      final var attrs = session.getAttributeNames().stream().collect(toMap(a -> a, session::getAttribute));
      session.invalidate(context.getExchange());
      final var newSession = get(context, true);
      attrs.forEach(newSession::setAttribute);
    }
    return true;
  }
}
