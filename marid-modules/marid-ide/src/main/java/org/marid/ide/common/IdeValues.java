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

import javafx.application.Platform;
import org.marid.IdePrefs;
import org.marid.jfx.LocalizedStrings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Locale;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
@Lazy(false)
public class IdeValues {

    public final String implementationVersion;

    @Autowired
    public IdeValues(@Value("${implementation.version}") String implementationVersion) {
        this.implementationVersion = implementationVersion;
    }

    @PostConstruct
    private void init() {
        IdePrefs.PREFERENCES.addPreferenceChangeListener(evt -> {
            if ("locale".equals(evt.getKey())) {
                final Locale locale = Locale.forLanguageTag(evt.getNewValue());
                if (locale != null && !Locale.ROOT.equals(locale) && !Locale.getDefault().equals(locale)) {
                    Locale.setDefault(locale);
                    if (Platform.isFxApplicationThread()) {
                        LocalizedStrings.LOCALE.set(locale);
                    } else {
                        Platform.runLater(() -> LocalizedStrings.LOCALE.set(locale));
                    }
                }
            }
        });
    }
}
