/*
 * Copyright (C) 2013 Dmitry Ovchinnikov
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

package org.marid.ide;

import org.marid.ide.itf.Application;
import org.marid.ide.swing.impl.ApplicationImpl;
import org.marid.util.Utils;

import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import static org.marid.methods.LogMethods.warning;
import static org.marid.methods.PrefMethods.preferences;

/**
 * @author Dmitry Ovchinnikov
 */
public class Ide {

    private static final Logger LOG = Logger.getLogger(MethodHandles.lookup().toString());
    public static final Application APPLICATION;

    static {
        final Preferences prefs = preferences("system");
        Application app;
        try {
            app = Utils.newInstance(Application.class, prefs.get("applicationClass", ApplicationImpl.class.getName()));
        } catch (Exception x) {
            warning(LOG, "Unable to create an application instance", x);
            app = new ApplicationImpl();
        }
        APPLICATION = app;
    }
}
