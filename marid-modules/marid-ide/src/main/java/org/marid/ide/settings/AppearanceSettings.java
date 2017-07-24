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

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class AppearanceSettings extends AbstractSettings {

    private final BooleanProperty showFullNames = new SimpleBooleanProperty(isShowFullNames());

    public AppearanceSettings() {
        preferences.addPreferenceChangeListener(evt -> {
            if ("showFullNames".equals(evt.getKey())) {
                Platform.runLater(() -> showFullNames.set(Boolean.parseBoolean(evt.getNewValue())));
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

    public void setShowFullNames(boolean value) {
        preferences.putBoolean("showFullNames", value);
    }

    public BooleanProperty showFullNamesProperty() {
        return showFullNames;
    }
}
