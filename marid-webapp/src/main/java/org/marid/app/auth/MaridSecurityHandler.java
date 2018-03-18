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
import io.undertow.server.HttpServerExchange;
import io.undertow.util.AttachmentKey;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.undertow.context.UndertowSessionStore;
import org.pac4j.undertow.context.UndertowWebContext;
import org.pac4j.undertow.util.UndertowHelper;

import java.util.LinkedHashMap;

import static io.undertow.util.AttachmentKey.create;

public class MaridSecurityHandler implements HttpHandler {

  public static final AttachmentKey<UndertowWebContext> WEB_CONTEXT_KEY = create(UndertowWebContext.class);

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
    final UndertowWebContext context = new UndertowWebContext(exchange, new UndertowSessionStore(exchange));
    exchange.putAttachment(WEB_CONTEXT_KEY, context);

    final Profiles<CommonProfile> manager = new Profiles<>(context);
    final LinkedHashMap<String, CommonProfile> profiles = manager.retrieveAll(true);

    UndertowHelper.populateContext(context, profiles);

    logic.perform(context, authorizers, clients);
  }

  private static class Profiles<U extends CommonProfile> extends ProfileManager<U> {

    public Profiles(WebContext context) {
      super(context);
    }

    @Override
    protected LinkedHashMap<String, U> retrieveAll(boolean readFromSession) {
      return super.retrieveAll(readFromSession);
    }
  }
}
