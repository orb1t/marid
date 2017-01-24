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

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.Locale;
import java.util.function.Supplier;

import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
public class LocalizedStrings {

    public static final ObjectProperty<Locale> LOCALE = new SimpleObjectProperty<>(Locale.getDefault());

    public static StringBinding ls(String text, Object... args) {
        return new LocalizedStringValue(() -> s(LOCALE.get(), text, args));
    }

    public static StringBinding fls(String format, String text, Object... args) {
        return new LocalizedStringValue(() -> {
            final String v = s(text, args);
            return String.format(LOCALE.get(), format, v);
        });
    }

    private static final class LocalizedStringValue extends StringBinding {

        private final Supplier<String> supplier;
        private final InvalidationListener listener;

        private LocalizedStringValue(Supplier<String> supplier) {
            this.supplier = supplier;
            this.listener = o -> invalidate();
            LOCALE.addListener(listener);
        }

        @Override
        protected String computeValue() {
            return supplier.get();
        }

        @Override
        protected void finalize() throws Throwable {
            try {
                Platform.runLater(this::dispose);
            } finally {
                super.finalize();
            }
        }

        @Override
        public void dispose() {
            LOCALE.removeListener(listener);
        }
    }
}
