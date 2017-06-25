package org.marid.dependant.settings;

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
