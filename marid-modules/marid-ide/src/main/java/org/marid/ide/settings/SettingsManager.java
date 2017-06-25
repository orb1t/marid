package org.marid.ide.settings;

import org.marid.IdeDependants;
import org.marid.dependant.settings.SettingsConfiguration;
import org.marid.jfx.action.FxAction;
import org.marid.spring.annotation.IdeAction;
import org.springframework.stereotype.Component;

import static org.marid.jfx.LocalizedStrings.ls;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class SettingsManager {

    @IdeAction
    public FxAction settingsAction(IdeDependants dependants) {
        return new FxAction("settings", "settings", "Tools")
                .setIcon("O_SETTINGS")
                .bindText(ls("Settings..."))
                .setEventHandler(event -> dependants.start(SettingsConfiguration.class, context -> {
                    context.setId("settingsEditor");
                    context.setDisplayName("Settings Editor");
                }));
    }
}
