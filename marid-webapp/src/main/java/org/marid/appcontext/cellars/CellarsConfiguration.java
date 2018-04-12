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

import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.RedirectHandler;
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
        .$(() -> stdLib.stdHead(b, h -> h.stylesheet("/user/css/cellars.css").title(c.s("cellars"))))
        .body(body -> body
            .div("", "cellars", cellars -> cellars
                .div("ui menu", "toolbar", toolbar -> toolbar
                    .a("item", "javascript:addCellar()", "", item -> item.i("plus circle icon"))
                    .a("item", "javascript:removeCellar()", "", item -> item.i("minus circle icon"))
                    .a("item", "javascript:editCellar()", "", item -> item.i("pencil alternate icon"))
                    .div("right menu", rm -> rm
                        .a("item", "/", "", item -> item.i("home icon"))
                    )
                )
                .div("ui middle aligned selection list segment", "list", list -> list
                    .div("")
                )
            )
            .$e("div", Map.of("id", "props", "class", "ui segment"))
            .$(() -> stdLib.viewScripts(body, "/user/js/cellars.js"))
        )
    );
  }

  @Bean
  public HttpHandler add(HttpExecutor executor) {
    return ex -> {
      if (Methods.GET.equals(ex.getRequestMethod())) {
        executor.form(ex, "add.html", "post", "addForm", "ui form modal", (c, b) -> b
            .i("close icon")
            .div("header", "", c.s("addCellar"))
            .div("content", content -> content
                .div("field", f -> f
                    .label(c.s("name"))
                    .input("name", "text", c.s("name"), "")
                )
            )
            .div("actions", actions -> actions
                .submitButton("ui positive button", c.s("add"))
                .div("ui deny button", "cancelButton", c.s("cancel"))
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
