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

package org.marid.wrapper;

import java.io.File;

/**
 * @author Dmitry Ovchinnikov
 */
public class ParseUtils {

    public static int getInt(String property, int def) {
        final String val = System.getProperty(property, System.getenv(property));
        try {
            return val == null ? def : Integer.parseInt(val);
        } catch (Exception x) {
            throw new IllegalStateException(property + "=" + val, x);
        }
    }

    public static long getLong(String property, long def) {
        final String val = System.getProperty(property, System.getenv(property));
        try {
            return val == null ? def : Long.parseLong(val);
        } catch (Exception x) {
            throw new IllegalStateException(property + "=" + val, x);
        }
    }

    public static File getFile(String property, File baseDir, String... def) {
        final String val = System.getProperty(property, System.getenv(property));
        try {
            File file;
            if (val != null) {
                file = new File(val);
            } else {
                file = baseDir == null ? new File(System.getProperty("user.home")) : baseDir;
                for (String name : def) {
                    file = new File(file, name);
                }
            }
            if (!file.getParentFile().isDirectory()) {
                if (!file.getParentFile().mkdirs()) {
                    throw new IllegalStateException("Unable to create " + file.getParent());
                }
            }
            return file;
        } catch (Exception x) {
            throw new IllegalStateException(property + "=" + val, x);
        }
    }

    public static File getDir(String property, File baseDir, String... def) {
        final String val = System.getProperty(property, System.getenv(property));
        try {
            File file;
            if (val != null) {
                file =  new File(val);
            } else {
                file = baseDir == null ? new File(System.getProperty("user.home")) : baseDir;
                for (String name : def) {
                    file = new File(file, name);
                }
            }
            if (!file.isDirectory()) {
                if (!file.mkdirs()) {
                    throw new IllegalStateException("Unable to create " + file);
                }
            }
            return file;
        } catch (Exception x) {
            throw new IllegalStateException(property + "=" + val, x);
        }
    }

    public static String getString(String property, String def) {
        final String val = System.getProperty(property, System.getenv(property));
        return val == null ? def : val;
    }
}
