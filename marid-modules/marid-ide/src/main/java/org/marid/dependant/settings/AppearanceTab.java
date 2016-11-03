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

package org.marid.dependant.settings;

import javafx.application.Application;
import javafx.scene.control.ComboBox;
import org.marid.IdePrefs;
import org.marid.ide.settings.AppearanceSettings;
import org.marid.jfx.LocalizedStrings;
import org.marid.jfx.panes.GenericGridPane;
import org.marid.logging.LogSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Locale;

import static javafx.application.Application.STYLESHEET_CASPIAN;
import static javafx.application.Application.STYLESHEET_MODENA;
import static javafx.collections.FXCollections.observableArrayList;
import static org.marid.Ide.ideLogHandler;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class AppearanceTab extends GenericGridPane implements SettingsEditor, LogSupport {

    private final AppearanceSettings appearanceSettings;

    @Autowired
    public AppearanceTab(AppearanceSettings appearanceSettings) {
        this.appearanceSettings = appearanceSettings;
        addControl("Locale", this::localeCombo);
        addIntField("Max log records", ideLogHandler::getMaxLogRecords, ideLogHandler::setMaxLogRecords, 100, 100_000, 100);
        addSeparator();
        addControl("System stylesheet", this::styleSheetCombo);
    }

    private ComboBox<String> styleSheetCombo() {
        final ComboBox<String> stylesheetCombo = new ComboBox<>(observableArrayList(STYLESHEET_CASPIAN, STYLESHEET_MODENA));
        stylesheetCombo.getSelectionModel().select(Application.getUserAgentStylesheet());
        stylesheetCombo.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            log(INFO, "Applying stylesheet {0}", newValue);
            Application.setUserAgentStylesheet(newValue);
            IdePrefs.PREFERENCES.put("style", newValue);
        });
        return stylesheetCombo;
    }

    private ComboBox<String> localeCombo() {
        final ComboBox<String> localeCombo = new ComboBox<>(observableArrayList("en", "fr", "es", "it"));
        localeCombo.setValue(LocalizedStrings.LOCALE.get().toLanguageTag());
        localeCombo.setEditable(true);
        localeCombo.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            final Locale locale = Locale.forLanguageTag(newValue);
            if (locale != null && !Locale.ROOT.equals(locale)) {
                IdePrefs.PREFERENCES.put("locale", locale.toLanguageTag());
            }
        });
        return localeCombo;
    }

    @Override
    public AppearanceSettings getSettings() {
        return appearanceSettings;
    }
}
