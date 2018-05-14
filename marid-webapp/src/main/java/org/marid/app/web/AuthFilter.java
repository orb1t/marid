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

import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormEncodedDataDefinition;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.servlet.spec.HttpServletRequestImpl;
import org.marid.app.annotation.PrototypeScoped;
import org.pac4j.core.client.Client;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.engine.DefaultCallbackLogic;
import org.pac4j.core.engine.DefaultLogoutLogic;
import org.pac4j.core.engine.DefaultSecurityLogic;
import org.pac4j.core.engine.decision.AlwaysUseSessionProfileStorageDecision;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.http.adapter.HttpActionAdapter;
import org.slf4j.Logger;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Component
@PrototypeScoped
@Order(1)
public class AuthFilter extends HttpFilter {

  private final Config config;
  private final DefaultSecurityLogic<Void, J2EContext> securityLogic;
  private final DefaultCallbackLogic<Void, J2EContext> callbackLogic;
  private final DefaultLogoutLogic<Void, J2EContext> logoutLogic;
  private final HttpActionAdapter<Void, J2EContext> unauthorizedLogic;
  private final FormParserFactory formParserFactory;
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
    this.callbackLogic = new DefaultCallbackLogic<>();
    this.logoutLogic = new DefaultLogoutLogic<>();
    this.formParserFactory = FormParserFactory.builder().addParser(new FormEncodedDataDefinition()).build();
    this.unauthorizedLogic = (code, ctx) -> null;

    this.securityLogic.setProfileStorageDecision(new AlwaysUseSessionProfileStorageDecision());
  }

  @Override
  public void doFilter(HttpServletRequest q, HttpServletResponse r, FilterChain c) throws IOException, ServletException {
    final var path = q.getServletPath();
    logger.debug("Request: {}", path);

    if (path.startsWith("/VAADIN/") || path.startsWith("/public/")) {
      super.doFilter(q, r, c);
      return;
    }

    final var request = (HttpServletRequestImpl) q;
    final var exchange = request.getExchange();
    final var securityContext = exchange.getSecurityContext();

    switch (path) {
      case "/Google2Client":
      case "/FacebookClient": {
        securityLogic.perform(new J2EContext(q, r), config, (ctx, profiles, params) -> {
          securityContext.authenticationComplete(new MaridAccount(profiles), "PAC4J_ACCOUNT", false);
          r.sendRedirect("/app");
          return null;
        }, unauthorizedLogic, path.substring(1), "user", null, false);
        return;
      }
      case "/callback": {
        final FormData formData = new FormData(request.getQueryParameters().size());
        request.getQueryParameters().forEach((k, v) -> formData.add(k, v.peek()));
        exchange.putAttachment(FormDataParser.FORM_DATA, formData);
        final var jc = new J2EContext(q, r);
        callbackLogic.perform(jc, config, (code, ctx) -> null, "/app", true, false, null, null);
        return;
      }
      case "/logout": {
        final var jc = new J2EContext(q, r);
        logoutLogic.perform(jc, config, (code, ctx) -> null, "/public/unauthorized.html", null, null, true, true);
        return;
      }
    }

    securityLogic.perform(new J2EContext(q, r), config, (ctx, profiles, params) -> {
      securityContext.authenticationComplete(new MaridAccount(profiles), "PAC4J_ACCOUNT", false);
      super.doFilter(q, r, c);
      return null;
    }, unauthorizedLogic, null, "user", null, false);
  }
}
