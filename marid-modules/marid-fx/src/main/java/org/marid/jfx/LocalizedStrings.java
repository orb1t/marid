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

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;
import org.marid.jfx.track.Tracks;

import java.util.Locale;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Locale.getDefault;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
public class LocalizedStrings {

  public static final ObjectProperty<Locale> LOCALE = new SimpleObjectProperty<>(null, "locale", getDefault());

  public static ObservableStringValue ls(String text, Object... args) {
    final Observable[] observables = Stream.of(args)
        .filter(Observable.class::isInstance)
        .map(Observable.class::cast)
        .toArray(Observable[]::new);
    return value(() -> {
      final Object[] params = Stream.of(args)
          .map(o -> o instanceof ObservableValue<?> ? ((ObservableValue<?>) o).getValue() : o)
          .toArray();
      return s(LOCALE.get(), text, params);
    }, observables);
  }

  public static ObservableStringValue fls(String format, String text, Object... args) {
    return Bindings.createStringBinding(() -> String.format(format, s(LOCALE.get(), text, args)), LOCALE);
  }

  private static ObservableStringValue value(Supplier<String> supplier, Observable... observables) {
    final SimpleStringProperty property = new SimpleStringProperty(supplier.get());
    final InvalidationListener listener = o -> property.set(supplier.get());
    LOCALE.addListener(listener);
    for (final Observable observable : observables) {
      observable.addListener(listener);
    }
    Tracks.CLEANER.register(property, () -> Platform.runLater(() -> {
      LOCALE.removeListener(listener);

      for (final Observable observable : observables) {
        observable.removeListener(listener);
      }
    }));
    return property;
  }
}
