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

package org.marid.ide.settings;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.marid.pref.PrefSupport;

import java.util.prefs.Preferences;

/**
 * @author Dmitry Ovchinnikov
 */
public class SettingsHolder implements PrefSupport {

    public final StringProperty snapshotUpdatePolicy;
    public final StringProperty releaseUpdatePolicy;
    public final StringProperty javaExecutable;
    public final ObjectProperty<String[]> javaArguments;

    private final Preferences preferences;

    public SettingsHolder(Preferences node) {
        preferences = node;
        snapshotUpdatePolicy = stringProperty("snapshotUpdatePolicy", null);
        releaseUpdatePolicy = stringProperty("releaseUpdatePolicy", null);
        javaExecutable = stringProperty("javaExecutable", "java");
        javaArguments = stringArrayProperty("javaArguments");
    }

    @Override
    public Preferences preferences() {
        return preferences;
    }

    private StringProperty stringProperty(String name, String defaultValue) {
        return new SimpleStringProperty(this, name, preferences.get(name, defaultValue));
    }

    private ObjectProperty<String[]> stringArrayProperty(String name, String... defaultValue) {
        return new SimpleObjectProperty<>(this, name, getPref(name, defaultValue));
    }

    private void save(StringProperty property) {
        if (property.getValue() != null && !property.getValue().isEmpty()) {
            preferences.put(property.getName(), property.getValue());
        } else {
            preferences.remove(property.getName());
        }
    }

    private void saveStringArray(ObjectProperty<String[]> property) {
        if (property.getValue() != null && property.getValue().length > 0) {
            putPref(property.getName(), property.getValue());
        } else {
            preferences.remove(property.getName());
        }
    }

    public void save() {
        save(snapshotUpdatePolicy);
        save(releaseUpdatePolicy);
        save(javaExecutable);
        saveStringArray(javaArguments);
    }
}
