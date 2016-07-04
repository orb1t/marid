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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class AbstractSettings {

    protected final Preferences preferences;

    public AbstractSettings(String name) {
        preferences = Preferences.userNodeForPackage(getClass()).node(getClass().getName()).node(name);
    }

    public String getName() {
        return preferences.name();
    }

    public byte[] save() {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            preferences.exportSubtree(bos);
        } catch (IOException | BackingStoreException x) {
            throw new IllegalStateException(x);
        }
        return bos.toByteArray();
    }

    public void load(byte[] data) {
        try {
            Preferences.importPreferences(new ByteArrayInputStream(data));
        } catch (IOException | InvalidPreferencesFormatException x) {
            throw new IllegalStateException(x);
        }
    }
}
