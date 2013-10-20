/*
 * Copyright (C) 2012 Dmitry Ovchinnikov
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
package org.marid.logging;

import org.marid.util.Utils;

import java.io.InputStream;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Base logging utilities.
 *
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public class Logging {

    public static final Level[] LEVELS = {
            Level.SEVERE,
            Level.WARNING,
            Level.CONFIG,
            Level.INFO,
            Level.FINE,
            Level.FINER,
            Level.FINEST
    };

    /**
     * Initializes the logging system.
     *
     * @param c Calling class.
     * @param res Log properties resource.
     */
    public static void init(Class<?> c, String res) {
        final ClassLoader cl = Utils.getClassLoader(c);
        try (InputStream is = cl.getResourceAsStream(res)) {
            final LogManager lm = LogManager.getLogManager();
            if (is != null) {
                lm.readConfiguration(is);
            }
            final String dynHandlers = lm.getProperty("dynHandlers");
            final Logger root = Logger.getGlobal().getParent();
            if (dynHandlers != null && root != null) {
                for (final String handler : dynHandlers.split(",")) {
                    if (handler.isEmpty()) {
                        continue;
                    }
                    try {
                        final Class<?> handlerClass = Class.forName(handler.trim(), true, cl);
                        root.addHandler((Handler)handlerClass.newInstance());
                    } catch (Exception x) {
                        x.printStackTrace(System.err);
                    }
                }
            }
        } catch (Exception x) {
            x.printStackTrace(System.err);
        }
    }
}
