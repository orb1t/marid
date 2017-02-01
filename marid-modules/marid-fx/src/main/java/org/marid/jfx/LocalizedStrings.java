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

import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;
import org.marid.jfx.beans.FxObject;

import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
public class LocalizedStrings {

    public static final FxObject<Locale> LOCALE = new FxObject<>(null, "locale", Locale.getDefault());

    public static ObservableValue<String> ls(String text, Object... args) {
        return new LocalizedStringValue(() -> s(LOCALE.get(), text, args));
    }

    public static ObservableValue<String> fls(String format, String text, Object... args) {
        return new LocalizedStringValue(() -> {
            final String v = s(text, args);
            return String.format(LOCALE.get(), format, v);
        });
    }

    private static final class LocalizedStringValue implements ObservableStringValue {

        private final Supplier<String> supplier;
        private final InvalidationListener listener;
        private final WeakInvalidationListener weakInvalidationListener;
        private final Collection<InvalidationListener> invalidationListeners = new ConcurrentLinkedQueue<>();
        private final Collection<ChangeListener<? super String>> changeListeners = new ConcurrentLinkedQueue<>();
        private final AtomicReference<String> ref;

        private LocalizedStringValue(Supplier<String> supplier) {
            this.supplier = supplier;
            this.ref = new AtomicReference<>(getValue());
            this.listener = o -> invalidate();
            this.weakInvalidationListener = new WeakInvalidationListener(listener);
            LOCALE.addListener(weakInvalidationListener);
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
        public String getValue() {
            return supplier.get();
        }

        @Override
        public void addListener(InvalidationListener listener) {
            invalidationListeners.add(listener);
        }

        @Override
        public void removeListener(InvalidationListener listener) {
            invalidationListeners.remove(listener);
        }

        private void invalidate() {
            final String old = ref.get();
            final String nev = getValue();
            if (ref.compareAndSet(old, nev)) {
                changeListeners.forEach(l -> l.changed(this, old, nev));
                invalidationListeners.forEach(l -> l.invalidated(this));
            }
        }

        @Override
        public String get() {
            return getValue();
        }
    }
}
