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

package org.marid.app.util;

import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.Session;
import io.undertow.server.session.SessionConfig;
import io.undertow.server.session.SessionManager;
import io.undertow.util.LocaleUtils;
import org.pac4j.core.profile.CommonProfile;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import static org.pac4j.core.context.Pac4jConstants.USER_PROFILES;

public interface ExchangeHelper {

  String USER_LOCALE_SESSION_KEY = "userLocale";

  static Stream<String> queryParams(HttpServerExchange exchange, String name) {
    return Stream.ofNullable(exchange.getQueryParameters().get(name)).flatMap(Collection::stream);
  }

  static Locale locale(HttpServerExchange exchange) {
    final var manager = exchange.getAttachment(SessionManager.ATTACHMENT_KEY);
    final var config = exchange.getAttachment(SessionConfig.ATTACHMENT_KEY);
    final var session = manager.getSession(exchange, config);
    {
      final Locale locale = (Locale) session.getAttribute(USER_LOCALE_SESSION_KEY);
      if (locale != null) {
        return locale;
      }
    }
    final Locale locale = LocaleUtils.getLocalesFromHeader(exchange.getRequestHeaders().getFirst("Accept-Language"))
        .stream()
        .findFirst()
        .orElse(Locale.US);
    session.setAttribute(USER_LOCALE_SESSION_KEY, locale);
    return locale;
  }

  static CommonProfile userProfile(Session session) {
    return (CommonProfile) ((Map) session.getAttribute(USER_PROFILES)).values().iterator().next();
  }
}
