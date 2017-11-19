/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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

  public AppearanceSettings() {
    preferences.addPreferenceChangeListener(evt -> {
      if (evt.getKey() != null) {
        switch (evt.getKey()) {
          case "showFullNames":
            Platform.runLater(() -> showFullNames.set(parseBoolean(evt.getNewValue())));
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

  public void setShowFullNames(boolean value) {
    preferences.putBoolean("showFullNames", value);
  }

  public BooleanProperty showFullNamesProperty() {
    return showFullNames;
  }
}
