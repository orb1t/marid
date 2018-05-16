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
package org.marid.ui.webide.base;

import com.vaadin.server.VaadinSession;
import org.marid.app.annotation.PrototypeScoped;
import org.marid.applib.l10n.Msg;
import org.marid.applib.l10n.Str;
import org.marid.l10n.L10n;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Locale;

@Component
@Import({ViewFactory.class, MainView.class})
public class UIConfiguration {

  @Bean
  @PrototypeScoped
  public Locale locale(VaadinSession session) {
    return session.getLocale();
  }

  @Bean
  @PrototypeScoped
  public Msg msg(InjectionPoint point, Locale locale) {
    {
      final var parameter = point.getMethodParameter();
      if (parameter != null) {
        final var key = parameter.getParameter().getName();
        return args -> L10n.m(locale, key, args);
      }
    }
    {
      final var field = point.getField();
      if (field != null) {
        final var key = field.getName();
        return args -> L10n.m(locale, key, args);
      }
    }
    return args -> "msg(" + Arrays.deepToString(args) + ")";
  }

  @Bean
  @PrototypeScoped
  public Str str(InjectionPoint point, Locale locale) {
    {
      final var parameter = point.getMethodParameter();
      if (parameter != null) {
        final var key = parameter.getParameter().getName();
        return args -> L10n.s(locale, key, args);
      }
    }
    {
      final var field = point.getField();
      if (field != null) {
        final var key = field.getName();
        return args -> L10n.s(locale, key, args);
      }
    }
    return args -> "str(" + Arrays.deepToString(args) + ")";
  }
}
