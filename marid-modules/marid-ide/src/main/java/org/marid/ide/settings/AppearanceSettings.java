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

package org.marid.ide.settings;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.springframework.stereotype.Component;

import static java.lang.Boolean.parseBoolean;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class AppearanceSettings extends AbstractSettings {

    private final BooleanProperty showFullNames = new SimpleBooleanProperty(isShowFullNames());
    private final BooleanProperty showGenericSignatures = new SimpleBooleanProperty(isShowGenericSignatures());

    public AppearanceSettings() {
        preferences.addPreferenceChangeListener(evt -> {
            if (evt.getKey() != null) {
                switch (evt.getKey()) {
                    case "showFullNames":
                        Platform.runLater(() -> showFullNames.set(parseBoolean(evt.getNewValue())));
                        break;
                    case "showGenericSignatures":
                        Platform.runLater(() -> showGenericSignatures.set(parseBoolean(evt.getNewValue())));
                        break;
                }
            }
        });
    }

    @Override
    public String getName() {
        return "Appearance";
    }

    public boolean isShowFullNames() {
        return preferences.getBoolean("showFullNames", false);
    }

    public boolean isShowGenericSignatures() {
        return preferences.getBoolean("showGenericSignatures", true);
    }

    public void setShowFullNames(boolean value) {
        preferences.putBoolean("showFullNames", value);
    }

    public void setShowGenericSignatures(boolean value) {
        preferences.putBoolean("showGenericSignatures", value);
    }

    public BooleanProperty showFullNamesProperty() {
        return showFullNames;
    }

    public BooleanProperty showGenericSignaturesProperty() {
        return showGenericSignatures;
    }
}
