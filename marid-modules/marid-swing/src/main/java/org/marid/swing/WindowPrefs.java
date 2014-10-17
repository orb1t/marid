/*
 * Copyright (C) 2014 Dmitry Ovchinnikov
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

package org.marid.swing;

import org.marid.pref.PrefUtils;

import java.awt.*;
import java.util.List;
import java.util.prefs.Preferences;

import static java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment;
import static java.util.Arrays.asList;

/**
 * @author Dmitry Ovchinnikov
 */
public class WindowPrefs {

    private static final Preferences WIN_PREFS = PrefUtils.preferences(WindowPrefs.class, "windows");

    public static void saveGraphicsDevice(Window window) {
        final List<GraphicsDevice> devices = asList(getLocalGraphicsEnvironment().getScreenDevices());
        final int index = devices.indexOf(window.getGraphicsConfiguration().getDevice());
        WIN_PREFS.putInt(window.getName(), index);
    }

    public static GraphicsConfiguration graphicsConfiguration(String name) {
        final int index = WIN_PREFS.getInt(name, -1);
        final GraphicsDevice[] graphicsDevices = getLocalGraphicsEnvironment().getScreenDevices();
        if (index >= 0 && index < graphicsDevices.length) {
            return graphicsDevices[index].getDefaultConfiguration();
        } else {
            return getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        }
    }
}
