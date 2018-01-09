/*-
 * #%L
 * marid-fx
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
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

package org.marid.jfx;

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;

import java.util.Locale;

import static java.util.Locale.getDefault;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
public class LocalizedStrings {

  public static final ObjectProperty<Locale> LOCALE = new SimpleObjectProperty<>(null, "locale", getDefault());

  public static ObservableStringValue ls(String text, Object... args) {
    return Bindings.createStringBinding(() -> {
      final Object[] params = of(args)
          .map(o -> o instanceof ObservableValue<?> ? ((ObservableValue<?>) o).getValue() : o)
          .toArray();
      return s(LOCALE.get(), text, params);
    }, concat(
        of(args).filter(Observable.class::isInstance).map(Observable.class::cast),
        of(LOCALE)
    ).toArray(Observable[]::new));
  }

  public static ObservableStringValue fls(String format, String text, Object... args) {
    return Bindings.createStringBinding(() -> String.format(format, s(LOCALE.get(), text, args)), LOCALE);
  }
}
