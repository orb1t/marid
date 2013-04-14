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

import org.marid.l10n.Localized;

import java.io.InputStream;
import java.util.logging.*;

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
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = c.getClassLoader();
        }
        try (InputStream is = cl.getResourceAsStream(res)) {
            LogManager lm = LogManager.getLogManager();
            if (is != null) {
                lm.readConfiguration(is);
            }
            String dynHandlers = lm.getProperty("dynHandlers");
            Logger root = Logger.getGlobal().getParent();
            if (dynHandlers != null && root != null) {
                for (String handler : dynHandlers.split(",")) {
                    if (handler.isEmpty()) {
                        continue;
                    }
                    handler = handler.trim();
                    try {
                        Class<?> handlerClass = Class.forName(handler, true, cl);
                        root.addHandler((Handler)handlerClass.newInstance());
                    } catch (Exception x) {
                        LogRecord lr = new LogRecord(Level.WARNING, "Load handler error");
                        lr.setResourceBundle(Localized.M.MB);
                        lr.setThrown(x);
                        root.log(lr);
                    }
                }
            }
        } catch (Exception x) {
            x.printStackTrace(System.err);
        }
    }
}
