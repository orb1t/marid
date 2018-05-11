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
package org.marid.app.templates;

import io.undertow.server.HttpHandler;
import io.undertow.util.HttpString;
import org.marid.app.util.ExchangeHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.OutputStreamWriter;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

@Component
public class HtmlHandlerFactory {

  private final TemplateEngine htmlTemplateEngine;

  @Autowired
  public HtmlHandlerFactory(TemplateEngine htmlTemplateEngine) {
    this.htmlTemplateEngine = htmlTemplateEngine;
  }

  public HttpHandler handler(String resource, Map<String, Object> vars) {
    return exchange -> {
      exchange.getResponseHeaders().add(new HttpString("Content-Type"), "text/html; charset=UTF-8");
      exchange.dispatch(() -> {
        exchange.startBlocking();
        final Context context = new Context(ExchangeHelper.locale(exchange), vars);
        context.setVariable("exchange", exchange);
        try {
          final OutputStreamWriter writer = new OutputStreamWriter(exchange.getOutputStream(), UTF_8);
          htmlTemplateEngine.process(resource, context, writer);
        } finally {
          exchange.endExchange();
        }
      });
    };
  }
}
