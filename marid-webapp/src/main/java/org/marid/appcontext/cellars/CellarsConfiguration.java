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

package org.marid.appcontext.cellars;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.RedirectHandler;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormEncodedDataDefinition;
import io.undertow.util.Methods;
import org.marid.app.html.StdLib;
import org.marid.app.http.HttpExecutor;
import org.marid.appcontext.session.view.ViewConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CellarsConfiguration implements ViewConfiguration {

  @Bean
  public HttpHandler manage(HttpExecutor executor, StdLib stdLib) {
    return exchange -> executor.html(exchange, (c, b) -> b
        .$(() -> stdLib.stdHead(b, c.s("cellars"), h -> h.stylesheet("/user/css/cellars.css")))
        .e("body", body -> body
            .e("div", Map.of("id", "cellars"), cellars -> cellars
                .e("div", Map.of("id", "toolbar", "class", "ui menu"), toolbar -> toolbar
                    .e("a", Map.of("class", "item", "href", "javascript:addCellar()"), item -> item
                        .e("i", Map.of("class", "plus circle icon"))
                    )
                    .e("a", Map.of("class", "item", "href", "javascript:removeCellar()"), item -> item
                        .e("i", Map.of("class", "minus circle icon"))
                    )
                    .e("a", Map.of("class", "item", "href", "javascript:editCellar()"), item -> item
                        .e("i", Map.of("class", "pencil alternate icon"))
                    )
                    .e("div", Map.of("class", "right menu"), rm -> rm
                        .e("a", Map.of("class", "item", "href", "/"), item -> item
                            .e("i", Map.of("class", "home icon"))
                        )
                    )
                )
                .e("div", Map.of("id", "list", "class", "ui middle aligned selection list segment"), list -> list
                    .e("div")
                )
            )
            .e("div", Map.of("id", "props", "class", "ui segment"))
            .$(v -> stdLib.viewScripts(v, "/user/js/cellars.js"))
        )
    );
  }

  @Bean
  public HttpHandler add(HttpExecutor executor) {
    return ex -> {
      if (Methods.GET.equals(ex.getRequestMethod())) {
        executor.form(ex, "add.html", "post", "addForm", "ui form modal", (c, b) -> b
            .e("i", Map.of("class", "close icon"))
            .e("div", c.s("addCellar"), Map.of("class", "header"))
            .e("div", Map.of("class", "content"), content -> content
                .e("div", Map.of("class", "field"), f -> f
                    .e("label", c.s("name"))
                    .e("input", Map.of("name", "name", "placeholder", c.s("name"), "type", "text"))
                )
            )
            .e("div", Map.of("class", "actions"), actions -> actions
                .e("input", Map.of("class", "ui positive button", "type", "submit", "value", c.s("add")))
                .e("div", c.s("cancel"), Map.of("class", "ui deny button"))
            )
        );
      } else {
        final var parser = new FormEncodedDataDefinition().create(ex);
        parser.parse(exchange -> {
          final var data = exchange.getAttachment(FormDataParser.FORM_DATA);
          if (data != null) {
            System.out.println(data.getFirst("name").getValue());
          }
          new RedirectHandler("/view/cellars/manage.html").handleRequest(exchange);
        });
      }
    };
  }
}
