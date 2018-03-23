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

import org.marid.xml.DomBuilder;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Consumer;

@Component
public class StdLib {

  @SafeVarargs
  public final DomBuilder stdHead(DomBuilder builder, Consumer<DomBuilder>... headConfigurers) {
    return stdHead(builder, null, headConfigurers);
  }

  @SafeVarargs
  public final DomBuilder stdHead(DomBuilder builder, String title, Consumer<DomBuilder>... headConfigurers) {
    final DomBuilder head = builder.e("head", h -> h
        .e("link", Map.of("rel", "icon", "href", "/marid-icon.gif", "type", "image/gif"))
        .meta("google", "notranslate")
        .meta("viewport", "width=device-width, initial-scale=1")
        .stylesheet("/user/semantic/semantic.css")
        .when(title != null, b -> b.e("title", title))
        .$(() -> {
          for (final Consumer<DomBuilder> configurer : headConfigurers) {
            configurer.accept(h);
          }
        })
    );
    return builder;
  }

  public final void scripts(DomBuilder builder, String... scripts) {
    builder.c("jQuery");
    builder.script("/user/jquery/jquery.js");
    builder.c("Semantic-UI");
    builder.script("/user/semantic/semantic.js");

    for (final String script : scripts) {
      builder.c(script);
      builder.script(script);
    }
  }
}
