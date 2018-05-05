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

import io.undertow.server.HttpHandler;
import org.pac4j.core.client.Client;
import org.pac4j.core.config.Config;
import org.pac4j.core.engine.DefaultSecurityLogic;
import org.pac4j.core.exception.HttpAction;

import java.util.List;

public class MaridSecurityLogic extends DefaultSecurityLogic<Void, MaridWebContext> {

  private final HttpHandler next;
  private final Config config;
  private final boolean processUnauthorized;

  public MaridSecurityLogic(HttpHandler next, Config config, boolean processUnauthorized) {
    this.next = next;
    this.config = config;
    this.processUnauthorized = processUnauthorized;

    setSaveProfileInSession(true);
  }

  @Override
  protected HttpAction unauthorized(MaridWebContext context, List<Client> currentClients) throws HttpAction {
    final HttpAction action = super.unauthorized(context, currentClients);
    if (processUnauthorized) {
      try {
        next.handleRequest(context.getExchange());
      } catch (RuntimeException x) {
        throw x;
      } catch (Exception x) {
        throw new IllegalStateException(x);
      }
    }
    return action;
  }

  public void perform(MaridWebContext context, String authorizers, String clients) {
    perform(context, config, this::process, (code, ctx) -> null, clients, authorizers, null, null);
  }

  private Void process(MaridWebContext context, Object... params) throws Throwable {
    next.handleRequest(context.getExchange());
    return null;
  }
}
