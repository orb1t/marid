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
import org.pac4j.core.engine.decision.AlwaysUseSessionProfileStorageDecision;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.CommonProfile;

import java.util.Collection;
import java.util.List;

public class MaridSecurityLogic extends DefaultSecurityLogic<Void, MaridWebContext> {

  private final HttpHandler next;
  private final Config config;

  public MaridSecurityLogic(HttpHandler next, Config config) {
    this.next = next;
    this.config = config;
    setProfileStorageDecision(new AlwaysUseSessionProfileStorageDecision());
  }

  @Override
  protected HttpAction unauthorized(MaridWebContext context, List<Client> currentClients) {
    return HttpAction.redirect(context, "/unauthorized");
  }

  private Void granted(MaridWebContext context, Collection<CommonProfile> profiles, Object... params) throws Exception {
    final var exchange = context.getExchange();
    exchange.getSecurityContext().authenticationComplete(new MaridAccount(profiles), "PAC4J_ACCOUNT", false);
    next.handleRequest(exchange);
    return null;
  }

  private Void adapt(int code, MaridWebContext context) {
    return null;
  }

  public HttpHandler handler(String authorizers, String clients) {
    return e -> perform(new MaridWebContext(e), config, this::granted, this::adapt, clients, authorizers, null, false);
  }
}
