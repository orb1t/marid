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

package org.marid.app.http;

import io.undertow.server.HttpServerExchange;
import org.marid.app.util.ExchangeHelper;
import org.marid.l10n.L10n;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

public final class HttpContext {

  private final HttpServerExchange exchange;

  private volatile Locale locale;

  public HttpContext(HttpServerExchange exchange) {
    this.exchange = exchange;
  }

  public OutputStream getOut() {
    return exchange.getOutputStream();
  }

  public InputStream getIn() {
    return exchange.getInputStream();
  }

  public String s(String pattern, Object... args) {
    return L10n.s(ExchangeHelper.locale(exchange), pattern, args);
  }

  public String m(String pattern, Object... args) {
    return L10n.m(ExchangeHelper.locale(exchange), pattern, args);
  }
}
