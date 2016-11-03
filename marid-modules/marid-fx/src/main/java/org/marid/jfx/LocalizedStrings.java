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

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableStringValue;

import java.util.Locale;

import static org.marid.l10n.L10n.m;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
public interface LocalizedStrings {

    ObjectProperty<Locale> LOCALE = new SimpleObjectProperty<>(Locale.getDefault());

    static ObservableStringValue ls(String text, Object... args) {
        return Bindings.createStringBinding(() -> s(LOCALE.get(), text, args), LOCALE);
    }

    static ObservableStringValue lm(String text, Object... args) {
        return Bindings.createStringBinding(() -> m(LOCALE.get(), text, args), LOCALE);
    }
}
