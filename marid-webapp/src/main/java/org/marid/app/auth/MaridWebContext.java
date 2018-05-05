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

import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.CookieImpl;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import org.pac4j.core.context.Cookie;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.core.util.JavaSerializationHelper;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.undertow.server.handlers.form.FormDataParser.FORM_DATA;

public class MaridWebContext implements WebContext {

  private static final JavaSerializationHelper JAVA_SERIALIZATION_HELPER = new JavaSerializationHelper();

  private final HttpServerExchange exchange;

  public MaridWebContext(HttpServerExchange exchange) {
    this.exchange = exchange;
  }

  public HttpServerExchange getExchange() {
    return exchange;
  }

  @Override
  public SessionStore<MaridWebContext> getSessionStore() {
    return new MaridSessionStore();
  }

  @Override
  public String getRequestParameter(String name) {
    final var param = exchange.getQueryParameters().get(name);
    if (param != null) {
      return param.peek();
    } else {
      final var formData = exchange.getAttachment(FORM_DATA);
      if (formData != null) {
        final var formParam = formData.get(name);
        if (formParam != null) {
          final var formParamValue = formParam.peek();
          if (formParamValue != null) {
            return formParamValue.getValue();
          }
        }
      }
      return null;
    }
  }

  @Override
  public Map<String, String[]> getRequestParameters() {
    final var result = new TreeMap<String, String[]>();

    final Function<String, BiFunction<String, String[], String[]>> applier = v -> (k, o) -> {
      if (v == null) {
        return o;
      } else {
        if (o == null) {
          return new String[]{v};
        } else {
          final var nv = Arrays.copyOf(o, o.length + 1);
          nv[o.length] = v;
          return nv;
        }
      }
    };
    exchange.getQueryParameters().forEach((k, vs) -> vs.forEach(v -> result.compute(k, applier.apply(v))));
    final var formData = exchange.getAttachment(FORM_DATA);
    if (formData != null) {
      formData.forEach(k -> {
        final var fvs = formData.get(k);
        if (fvs != null) {
          for (final var fv : fvs) {
            result.compute(k, applier.apply(fv.getValue()));
          }
        }
      });
    }

    return result;
  }

  @Override
  public Object getRequestAttribute(String name) {
    final var param = exchange.getPathParameters().get(name);
    if (param != null) {
      final var first = param.getFirst();
      if (first != null) {
        return JAVA_SERIALIZATION_HELPER.unserializeFromBase64(first);
      }
    }
    return null;
  }

  @Override
  public void setRequestAttribute(String name, Object value) {
    if (value != null) {
      exchange.addPathParam(name, JAVA_SERIALIZATION_HELPER.serializeToBase64((Serializable) value));
    }
  }

  @Override
  public String getRequestHeader(String name) {
    return exchange.getRequestHeaders().get(name, 0);
  }

  @Override
  public String getRequestMethod() {
    return exchange.getRequestMethod().toString();
  }

  @Override
  public String getRemoteAddr() {
    return exchange.getSourceAddress().getAddress().getHostAddress();
  }

  @Override
  public void writeResponseContent(String content) {
    exchange.getResponseSender().send(content);
  }

  @Override
  public void setResponseStatus(int code) {
    exchange.setStatusCode(code);
  }

  @Override
  public void setResponseHeader(String name, String value) {
    exchange.getResponseHeaders().put(HttpString.tryFromString(name), value);
  }

  @Override
  public void setResponseContentType(String content) {
    exchange.getResponseHeaders().add(Headers.CONTENT_TYPE, content);
  }

  @Override
  public String getServerName() {
    return exchange.getHostName();
  }

  @Override
  public int getServerPort() {
    return exchange.getHostPort();
  }

  @Override
  public String getScheme() {
    return exchange.getRequestScheme();
  }

  @Override
  public boolean isSecure() {
    return exchange.isSecure();
  }

  @Override
  public String getFullRequestURL() {
    if (CommonHelper.isNotBlank(exchange.getQueryString())) {
      return exchange.getRequestURL() + "?" + exchange.getQueryString();
    } else {
      return exchange.getRequestURL();
    }
  }

  @Override
  public Collection<Cookie> getRequestCookies() {
    return exchange.getRequestCookies().values().stream()
        .map(c -> {
          final var cookie = new Cookie(c.getName(), c.getValue());

          cookie.setComment(c.getComment());
          cookie.setDomain(c.getDomain());
          cookie.setHttpOnly(c.isHttpOnly());
          cookie.setPath(c.getPath());
          cookie.setSecure(c.isSecure());

          if (c.getMaxAge() != null) {
            cookie.setMaxAge(c.getMaxAge());
          }

          return cookie;
        })
        .collect(Collectors.toList());
  }

  @Override
  public void addResponseCookie(Cookie cookie) {
    final var c = new CookieImpl(cookie.getName(), cookie.getValue());

    c.setComment(cookie.getComment());
    c.setDomain(cookie.getDomain());
    c.setPath(cookie.getPath());
    c.setVersion(cookie.getVersion());

    if (cookie.getMaxAge() >= 0) {
      c.setMaxAge(cookie.getMaxAge());
    }

    c.setHttpOnly(cookie.isHttpOnly());
    c.setSecure(cookie.isSecure());

    exchange.setResponseCookie(c);
  }

  @Override
  public String getPath() {
    return exchange.getRequestPath();
  }

  @Override
  public String toString() {
    return exchange.toString();
  }
}
