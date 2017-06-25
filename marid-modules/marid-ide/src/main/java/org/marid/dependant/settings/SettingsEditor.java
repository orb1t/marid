package org.marid.dependant.settings;

import javafx.scene.Node;
import org.marid.ide.settings.AbstractSettings;

/**
 * @author Dmitry Ovchinnikov
 */
public interface SettingsEditor {

    AbstractSettings getSettings();

    default Node getNode() {
        return (Node) this;
    }
}
