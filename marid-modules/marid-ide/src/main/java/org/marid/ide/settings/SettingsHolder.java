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

    public SettingsHolder(Preferences node) {
        snapshotUpdatePolicy = stringProperty("snapshotUpdatePolicy", node, null);
        releaseUpdatePolicy = stringProperty("releaseUpdatePolicy", node, null);
    }

    private StringProperty stringProperty(String name, Preferences node, String defaultValue) {
        return new SimpleStringProperty(this, name, node.get(name, defaultValue));
    }

    private void save(StringProperty property, Preferences node) {
        if (property.getValue() != null && !property.getValue().isEmpty()) {
            node.put(property.getName(), property.getValue());
        } else {
            node.remove(property.getName());
        }
    }

    public void save(Preferences node) {
        save(snapshotUpdatePolicy, node);
        save(releaseUpdatePolicy, node);
    }
}
