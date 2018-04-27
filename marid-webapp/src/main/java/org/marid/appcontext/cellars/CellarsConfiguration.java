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
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.util.Deque;
import java.util.Map;

@Component
@ComponentScan
public class CellarsConfiguration implements ViewConfiguration {

  @Bean
  public HttpHandler manage(HttpExecutor executor, StdLib stdLib, Cellars cellars) {
    return exchange -> executor.html(exchange, (c, b) -> b
        .$(() -> stdLib.stdHead(c, b, h -> h.stylesheet("/user/css/cellars.css").title(c.s("cellars"))))
        .body("p-2", body -> body
            .div("btn-toolbar", "toolbar", toolbar -> toolbar
                .div("btn-group btn-group-lg mr-2", g -> g
                    .button("btn btn-secondary", $ -> $.$a("onclick", "addCellar()").i("ion-ios-add"))
                    .button("btn btn-secondary", $ -> $.$a("onclick", "removeCellar()").i("ion-ios-remove"))
                    .button("btn btn-secondary", $ -> $.$a("onclick", "editCellar()").i("ion-ios-settings"))
                )
                .div("btn-group btn-group-lg", g -> g
                    .button("btn btn-secondary", $ -> $.$a("onclick", "window.location = '/'").i("ion-ios-home"))
                )
            )
            .div("list-group mt-2", "list", list -> cellars.cellars().forEach(e -> {
              list.div("list-group-item", "cellar" + e, e);
            }))
            .$(() -> stdLib.scripts(body, "/user/js/cellars.js"))
        )
    );
  }

  @Bean
  public HttpHandler add(HttpExecutor executor, Cellars cellars) {
    return ex -> {
      if (Methods.GET.equals(ex.getRequestMethod())) {
        executor.fragment(ex, "div", Map.of("class", "modal fade"), (c, modal) -> modal
            .div("modal-dialog modal-dialog-centered", mdl -> mdl
                .form("add.html", "post", "addForm", "modal-content", mdc -> mdc
                    .div("modal-header", mdh -> mdh
                        .h5("modal-title", c.s("addCellar"))
                    )
                    .div("modal-body", bd -> bd
                        .div("form-group", g -> g
                            .label(c.s("name"), "name")
                            .input("name", "text", c.s("name"), "")
                        )
                    )
                    .div("modal-footer", ft -> ft
                        .button("btn btn-secondary", b -> b.$t(c.s("cancel")).$a("data-dismiss", "modal"))
                        .button("btn btn-primary", b -> b.$t(c.s("add")).$a("type", "submit"))
                    )
                )
            )
        );
      } else {
        final var parser = new FormEncodedDataDefinition().create(ex);
        parser.parse(exchange -> {
          final var data = exchange.getAttachment(FormDataParser.FORM_DATA);
          if (data != null) {
            final String name = data.getFirst("name").getValue();
            cellars.add(name);
          }
          new RedirectHandler("manage.html").handleRequest(exchange);
        });
      }
    };
  }

  @Bean
  public HttpHandler delete(Cellars cellars) {
    return ex -> {
      final Deque<String> names = ex.getQueryParameters().get("name");
      final String name = names == null ? null : names.poll();
      cellars.delete(name);
      new RedirectHandler("manage.html").handleRequest(ex);
    };
  }
}
