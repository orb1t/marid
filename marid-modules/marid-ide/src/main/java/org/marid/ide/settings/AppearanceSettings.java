package org.marid.ide.settings;

import org.springframework.stereotype.Component;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class AppearanceSettings extends AbstractSettings {

    @Override
    public String getName() {
        return "Appearance";
    }
}
