package org.marid.dependant.settings;

/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import javafx.application.Application;
import javafx.scene.control.ComboBox;
import org.marid.IdePrefs;
import org.marid.ide.logging.IdeLogHandler;
import org.marid.ide.settings.AppearanceSettings;
import org.marid.jfx.LocalizedStrings;
import org.marid.jfx.panes.GenericGridPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Locale;

import static java.util.logging.Level.INFO;
import static javafx.application.Application.STYLESHEET_CASPIAN;
import static javafx.application.Application.STYLESHEET_MODENA;
import static javafx.collections.FXCollections.observableArrayList;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class AppearanceTab extends GenericGridPane implements SettingsEditor {

    private final AppearanceSettings appearanceSettings;

    @Autowired
    public AppearanceTab(AppearanceSettings appearanceSettings, IdeLogHandler logHandler) {
        this.appearanceSettings = appearanceSettings;
        addControl("Locale", this::localeCombo);
        addIntField("Max log records", logHandler::getMaxRecords, logHandler::setMaxRecords, 100, 100_000, 100);
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
        final ComboBox<String> localeCombo = new ComboBox<>(observableArrayList("en", "fr", "es", "it", "ru"));
        localeCombo.setEditable(true);
        localeCombo.getSelectionModel().select(LocalizedStrings.LOCALE.get().toLanguageTag());
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
