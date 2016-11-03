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

package org.marid.ide.common;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableStringValue;
import org.marid.IdePrefs;
import org.marid.l10n.L10n;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Locale;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class IdeValues {

    public static final ObjectProperty<Locale> LOCALE = new SimpleObjectProperty<>(Locale.getDefault());

    public final String implementationVersion;

    @Autowired
    public IdeValues(@Value("${implementation.version}") String implementationVersion) {
        this.implementationVersion = implementationVersion;
    }

    @PostConstruct
    private void init() {
        IdePrefs.PREFERENCES.addPreferenceChangeListener(evt -> {
            switch (evt.getKey()) {
                case "locale":
                    LOCALE.set(Locale.forLanguageTag(evt.getNewValue()));
                    break;
            }
        });
    }

    public static ObservableStringValue ls(String text, Object... args) {
        return Bindings.createStringBinding(() -> L10n.s(LOCALE.get(), text, args), LOCALE);
    }

    public static ObservableStringValue lm(String text, Object... args) {
        return Bindings.createStringBinding(() -> L10n.m(LOCALE.get(), text, args), LOCALE);
    }
}
