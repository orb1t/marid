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
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableStringValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.marid.l10n.L10n.m;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
public class LocalizedStrings {

    public static final ObjectProperty<Locale> LOCALE = new SimpleObjectProperty<>(Locale.getDefault());

    public static ObservableStringValue ls(String text, Object... args) {
        return new LocalizedStringValue(() -> s(LOCALE.get(), text, args));
    }

    public static ObservableStringValue lm(String text, Object... args) {
        return new LocalizedStringValue(() -> m(LOCALE.get(), text, args));
    }

    public static ObservableStringValue fls(String format, String text, Object... args) {
        return new LocalizedStringValue(() -> {
            final String v = s(text, args);
            return String.format(LOCALE.get(), format, v);
        });
    }

    public static ObservableStringValue flm(String format, String text, Object... args) {
        return new LocalizedStringValue(() -> {
            final String v = m(LOCALE.get(), text, args);
            return String.format(LOCALE.get(), format, v);
        });
    }

    private static final class LocalizedStringValue extends StringExpression implements InvalidationListener {

        private final Supplier<String> supplier;
        private final List<ChangeListener<? super String>> changeListeners = new ArrayList<>();
        private final List<InvalidationListener> invalidationListeners = new ArrayList<>();
        private final AtomicReference<String> value;
        private final WeakInvalidationListener invalidationListener;

        private LocalizedStringValue(Supplier<String> supplier) {
            this.supplier = supplier;
            this.value = new AtomicReference<>(get());
            LOCALE.addListener(invalidationListener = new WeakInvalidationListener(this));
        }

        @Override
        public String get() {
            return supplier.get();
        }

        @Override
        public void addListener(ChangeListener<? super String> listener) {
            changeListeners.add(listener);
        }

        @Override
        public void removeListener(ChangeListener<? super String> listener) {
            changeListeners.remove(listener);
        }

        @Override
        public void addListener(InvalidationListener listener) {
            invalidationListeners.add(listener);
        }

        @Override
        public void removeListener(InvalidationListener listener) {
            invalidationListeners.remove(listener);
        }

        @Override
        protected void finalize() throws Throwable {
            try {
                Platform.runLater(() -> LOCALE.removeListener(invalidationListener));
            } finally {
                super.finalize();
            }
        }

        @Override
        public void invalidated(Observable observable) {
            final String oldValue = value.get();
            final String newValue = get();
            if (!Objects.equals(oldValue, newValue)) {
                invalidationListeners.forEach(l -> l.invalidated(this));
                changeListeners.forEach(l -> l.changed(this, oldValue, newValue));
            }
        }
    }
}
