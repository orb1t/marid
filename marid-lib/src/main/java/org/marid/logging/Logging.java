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

import java.io.InputStream;
import java.util.logging.LogManager;

/**
 * Base logging utilities.
 *
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public class Logging {

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
        } catch (Exception x) {
            x.printStackTrace(System.err);
        }
    }
}
