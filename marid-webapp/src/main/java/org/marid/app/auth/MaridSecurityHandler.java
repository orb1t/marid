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

import io.undertow.security.handlers.SecurityInitialHandler;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.AttachmentKey;
import org.pac4j.core.profile.CommonProfile;

import java.util.LinkedHashMap;

import static io.undertow.security.api.AuthenticationMode.PRO_ACTIVE;
import static io.undertow.util.AttachmentKey.create;

public class MaridSecurityHandler implements HttpHandler {

  public static final AttachmentKey<MaridWebContext> WEB_CONTEXT_KEY = create(MaridWebContext.class);

  private final MaridSecurityLogic logic;
  private final String authorizers;
  private final String clients;

  public MaridSecurityHandler(MaridSecurityLogic logic, String authorizers, String clients) {
    this.logic = logic;
    this.authorizers = authorizers;
    this.clients = clients;
  }

  @Override
  public void handleRequest(HttpServerExchange exchange) {
    final MaridWebContext context = new MaridWebContext(exchange);
    exchange.putAttachment(WEB_CONTEXT_KEY, context);

    final MaridProfileManager<CommonProfile> manager = new MaridProfileManager<>(context);
    final LinkedHashMap<String, CommonProfile> profiles = manager.retrieveAll(true);

    if (profiles != null && !profiles.isEmpty()) {
      if (exchange.getSecurityContext() == null) {
        exchange.setSecurityContext(new SecurityInitialHandler(PRO_ACTIVE, null, null).createSecurityContext(exchange));
      }

      exchange.getSecurityContext().authenticationComplete(new MaridAccount(profiles), "PAC4J_ACCOUNT", false);
    }

    logic.perform(context, authorizers, clients);
  }
}
