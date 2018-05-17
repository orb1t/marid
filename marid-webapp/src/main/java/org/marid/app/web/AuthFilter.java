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
package org.marid.app.web;

import io.undertow.servlet.spec.HttpServletRequestImpl;
import org.marid.app.annotation.PrototypeScoped;
import org.pac4j.core.client.Client;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.engine.DefaultSecurityLogic;
import org.pac4j.core.engine.decision.AlwaysUseSessionProfileStorageDecision;
import org.pac4j.core.exception.HttpAction;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Component
@PrototypeScoped
public class AuthFilter extends HttpFilter {

  private final Config config;
  private final DefaultSecurityLogic<Void, J2EContext> securityLogic;
  private final Logger logger;

  public AuthFilter(Config config, Logger logger) {
    this.config = config;
    this.logger = logger;
    this.securityLogic = new DefaultSecurityLogic<>() {
      @Override
      protected HttpAction unauthorized(J2EContext context, List<Client> currentClients) {
        return HttpAction.redirect(context, "/public/unauthorized.html");
      }
    };
    this.securityLogic.setProfileStorageDecision(new AlwaysUseSessionProfileStorageDecision());
  }

  @Override
  public void doFilter(HttpServletRequest q, HttpServletResponse r, FilterChain c) {
    logger.debug("Request: {}?{}", q.getRequestURI(), q.getQueryString());

    final var request = (HttpServletRequestImpl) q;
    final var exchange = request.getExchange();
    final var securityContext = exchange.getSecurityContext();

    securityLogic.perform(new J2EContext(q, r), config, (ctx, profiles, params) -> {
      securityContext.authenticationComplete(new MaridAccount(profiles), "PAC4J_ACCOUNT", false);
      super.doFilter(q, r, c);
      return null;
    }, (code, ctx) -> null, null, "user", null, false);
  }
}
