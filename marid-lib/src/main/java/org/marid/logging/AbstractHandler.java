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

package org.marid.logging;

import java.util.logging.*;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class AbstractHandler extends Handler {

    protected final LogManager manager = LogManager.getLogManager();

    public AbstractHandler() throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = getClass().getClassLoader();
        }
        String level = manager.getProperty(getClass().getCanonicalName() + ".level");
        if (level != null) {
            setLevel(Level.parse(level));
        }
        String filter = manager.getProperty(getClass().getCanonicalName() + ".filter");
        if (filter != null) {
            setFilter((Filter)cl.loadClass(filter).newInstance());
        }
        String formatter = manager.getProperty(getClass().getCanonicalName() + ".formatter");
        if (formatter != null) {
            setFormatter((Formatter)cl.loadClass(formatter).newInstance());
        }
        String errorManager = manager.getProperty(getClass().getCanonicalName() + ".errorManager");
        if (errorManager != null) {
            setErrorManager((ErrorManager)cl.loadClass(errorManager).newInstance());
        }
        String encoding = manager.getProperty(getClass().getCanonicalName() + ".encoding");
        if (encoding != null) {
            setEncoding(encoding);
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }
}
