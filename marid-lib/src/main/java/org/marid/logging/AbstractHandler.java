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
 * Abstract log handler.
 *
 * @author Dmitry Ovchinnikov
 */
public abstract class AbstractHandler extends Handler {

    protected final LogManager manager = LogManager.getLogManager();

    @SuppressWarnings("ConstantConditions")
    public AbstractHandler() throws Exception {
        String level = manager.getProperty(getClass().getName() + ".level");
        String encoding = manager.getProperty(getClass().getName() + "encoding");
        if (level != null) {
            setLevel(Level.parse(level.trim().toUpperCase()));
        }
        if (encoding != null) {
            setEncoding(encoding);
        }
        String filterClass = manager.getProperty(getClass().getName() + ".filter");
        if (filterClass != null) {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (cl != null) {
                cl = getClass().getClassLoader();
            }
            setFilter((Filter)cl.loadClass(filterClass).newInstance());
        }
        String formatterClass = manager.getProperty(getClass().getName() + ".formatter");
        if (formatterClass != null) {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (cl != null) {
                cl = getClass().getClassLoader();
            }
            setFormatter((Formatter)cl.loadClass(formatterClass).newInstance());
        }
    }
}
