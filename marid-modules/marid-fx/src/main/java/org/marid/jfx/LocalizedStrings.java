/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
 * Marid, the free data acquisition and visualization software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.marid.jfx;

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import org.marid.jfx.beans.FxObject;

import java.util.Locale;
import java.util.stream.Stream;

import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
public class LocalizedStrings {

    public static final FxObject<Locale> LOCALE = new FxObject<>(null, "locale", Locale.getDefault());

    public static ObservableValue<String> ls(String text, Object... args) {
        return Bindings.createStringBinding(() -> s(LOCALE.get(), text, args), LOCALE);
    }

    public static ObservableValue<String> fls(String format, String text, Object... args) {
        return Bindings.createStringBinding(() -> String.format(format, s(LOCALE.get(), text, args)), LOCALE);
    }

    public static ObservableValue<String> fs(String text, Object... args) {
        final Observable[] observables = Stream.concat(
                Stream.of(LOCALE),
                Stream.of(args).filter(Observable.class::isInstance).map(Observable.class::cast)
        ).toArray(Observable[]::new);
        return Bindings.createStringBinding(() -> {
            final Object[] params = Stream.of(args)
                    .map(o -> o instanceof ObservableValue<?> ? ((ObservableValue<?>) o).getValue() : o)
                    .toArray();
            return s(LOCALE.get(), text, params);
        }, observables);
    }
}
