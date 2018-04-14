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

import org.marid.xml.HtmlBuilder;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class StdLib {

  @SafeVarargs
  public final void stdHead(HtmlBuilder builder, Consumer<HtmlBuilder>... headConfigurers) {
    builder.head(h -> h
        .link("icon", "/marid-icon.gif", "image/gif")
        .script("/user/jquery/jquery.js")
        .script("/user/js/baseview.js")
        .meta("google", "notranslate")
        .meta("viewport", "width=device-width, initial-scale=1")
        .stylesheet("/user/semantic/cosmo.css")
        .$(() -> {
          for (final Consumer<HtmlBuilder> configurer : headConfigurers) {
            configurer.accept(h);
          }
        }));
  }

  public final void scripts(HtmlBuilder builder, String... scripts) {
    builder.script("/user/semantic/semantic.js");

    for (final String script : scripts) {
      builder.script(script);
    }
  }
}
