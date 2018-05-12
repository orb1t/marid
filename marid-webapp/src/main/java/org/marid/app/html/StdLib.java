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

package org.marid.app.html;

import io.undertow.server.HttpServerExchange;
import org.intellij.lang.annotations.Language;
import org.marid.xml.HtmlBuilder;

import java.util.function.Consumer;

public class StdLib extends BaseLib {

  public StdLib(HttpServerExchange exchange) {
    super(exchange);
  }

  @SafeVarargs
  public final void stdHead(HtmlBuilder builder, Consumer<HtmlBuilder>... headConfigurers) {
    builder.head(h -> h
        .link("icon", "/marid-icon.gif", "image/gif")
        .script("/user/js/baseview.js")
        .meta("google", "notranslate")
        .meta("viewport", "width=device-width, initial-scale=1")
        .stylesheet(
            "https://stackpath.bootstrapcdn.com/bootstrap/4.1.1/css/bootstrap.min.css",
            "sha384-WskhaSGFgHYWDcbwN70/dfYBj47jz9qbsMId/iRN3ewGhXQFZCSftd1LZCfmhktB"
        )
        .stylesheet(
            "https://cdnjs.cloudflare.com/ajax/libs/ionicons/4.0.0-19/css/ionicons.min.css",
            "sha256-+nvKDBDNZ4sWZvY5H2iCgS9kttBH5n3YWwdBmkeQYRQ="
        )
        .script(
            "https://cdnjs.cloudflare.com/ajax/libs/jquery/3.3.1/jquery.min.js",
            "sha256-FgpCb/KJQlLNfOu91ta32o/NMZxltwRo8QtmkMRdAu8="
        )
        .script(
            "https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.3/umd/popper.min.js",
            "sha384-ZMP7rVo3mIykV+2+9J3UJ46jBk0WLaUAdn689aCwoqbBJiSnjAK/l8WvCWPIPm49"
        )
        .script(
            "https://stackpath.bootstrapcdn.com/bootstrap/4.1.1/js/bootstrap.min.js",
            "sha384-smHYKdLADwkXOn1EmN1qk/HfnUcbVRZyYmZ4qpPea6sjB/pTJ0euyQp0Mk8ck+5T"
        )
        .$(() -> {
          for (final Consumer<HtmlBuilder> configurer : headConfigurers) {
            configurer.accept(h);
          }
        }));
  }

  public final void scripts(HtmlBuilder builder,
                            @Language(value = "HTML", prefix = "<script src='", suffix = "'>") String... scripts) {
    for (@Language(value = "HTML", prefix = "<script src='", suffix = "'>") final String script : scripts) {
      builder.script(script);
    }
  }
}
